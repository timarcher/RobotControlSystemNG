package com.timarcher.robotcontrolsystemng.services;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.exception.ConnectionLostException;

import android.util.Log;

import com.timarcher.robotcontrolsystemng.robot.ioio.RobotIOIOInterface;

/**
 * This is the class which is responsible for controlling the robots 
 * main drive motors.
 * 
 * For this to work, the IOIOLibAndroid project was modified.
 * ioio.lib.impl.DigitalInputImpl and ioio.lib.api.DigitalInput were
 * both modified to count pulses and expose a getPulseCount and clearPulseCount methods.
 *    
 * 
 * This class is used to interface with a Magnevation motor driver board through the IOIO board. 
 * The motor driver board uses two LMD18200T components to drive the motors.
 * 
 * Magnevation Motor Driver Board Notes:
 * Motor Driver Board Controls and Component Connections
 * 
 * Pinout when looking at the Magnevation board from above.
 * 39                  1
 * --------------------
 * --------------------
 * 40                  2
 * 
 *                       Motor Board
 * Function					Pin				IOIO Pin
 * -----------------------------------------------------
 * Right Direction			36					9
 * Right Brake				40					11
 * Right Speed				29					7
 * Right Thermal Flag		32					13
 * Right Current Sensing	7					32
 * 
 * Left Direction			34					8
 * Left Brake				38					10
 * Left Speed				27					6
 * Left Thermal Flag		30					12
 * Left Current Sensing		9					31
 * 
 * Left Encoder(1)								14
 * Left Encoder(2)								15
 * Right Encoder(1)								16
 * Right Encoder(2)								17
 * 
 * 
 * To enable current sensing, jumper R10 and R11
 * To enable the thermal flags, jumper R2 and R3
 * To enable the motor driver board to power the OOPIC, jumper R13
 * PWM Can be enabled on IOIO Pins 3-7, 10-14, 18-26, and 47-48
 * 
 * 
 * '''''''''''''''''''' LMD18200 Notes ''''''''''''''''''''''''' 
 * The LMD18200 is a 3A H-Bridge designed for motion control applications. 
 * The device is built using a multi-technology process which combines 
 * bipolar and CMOS control circuitry with DMOS power devices on the same 
 * monolithic structure. Ideal for driving DC and stepper motors; the 
 * LMD18200 accommodates peak output currents up to 6A. An innovative 
 * circuit which facilitates low-loss sensing of the output current has 
 * been implemented.
 * 
 * Delivers up to 3A continuous output 
 * Operates at supply voltages up to 55V 
 * Low RDS(ON) typically 0.3Ohm per switch 
 * TTL and CMOS compatible inputs 
 * No "shoot-through" current 
 * Thermal warning flag output at 145�C 
 * Thermal shutdown (outputs off) at 170�C 
 * Internal clamp diodes 
 * Shorted load protection 
 * Internal charge pump with external bootstrap capability   
 * 
 */
public class MotorControlService {
	/** Logging tag. */
	protected static final String LOGTAG = "MotorControlService";
	/** A thread which monitors the motors. */
	Thread motorMonitorThread;	
	/** The IOIO interface class. */
	RobotIOIOInterface ioio;

	/** Boolean as to if the robot is currently moving or not. */
	protected boolean _isRobotMoving = false;
	
	/** How long the motor monitor thread should sleep for between runs. */
	protected static final int MOTOR_MONITOR_THREAD_SLEEP_MS = 50;
	/** Max # of encoder clicks per speed control interval at max speed. This is dependent on how often the motor
	 * monitor thread runs. This is how many clicks should be expected per interval at MAX speed.
	 * 150 encoder clicks seems to be possible at max speed when the thread polls every 100ms
	 * 70 encoder clicks seems to be possible at max speed when the thread polls every 50ms
	 */
	protected static final int MAX_SPEED_PER_INTERVAL = 68; //400;
	/** The max speed of the motor. */
	protected static final int MAX_MOTOR_SPEED = 100;
	/** The speed of the motor to slow down to when positioning the robot and nearing its destination distance/degrees to turn. */
	protected static final int SLOW_MOTOR_POSITIONING_SPEED = 25;
	/** The number of encoder clicks that occur per degree of turning the robot. */
	//protected static final double ENCODER_CLICKS_PER_TURN_DEGREE = 4.701;
	protected static final double ENCODER_CLICKS_PER_TURN_DEGREE = 4.85;
	/** Robot travels .0459 centimeters per shaft click */
	protected static final double ENCODER_CLICKS_PER_CM = 0.0459;
			
	/** The number of right motor encoder clicks to move. */
	protected int rightClicksToMove = 0;
	/** The number of left motor encoder clicks to move. */
	protected int leftClicksToMove = 0;
	/** The velocity to move the left motor at: 0 to 100 */
	protected double leftMotorVelocity = 0;
	/** The velocity to move the right motor at: 0 to 100 */
	protected double rightMotorVelocity = 0;
	/** Desired velocity clicks per interval we want to achieve. */
	protected int desiredVelocityClicks = 0;
    /** Desired bias clicks per interval we want to achieve. */
	protected int desiredBiasClicks = 0;

	/** The number of left encoder clicks the robot has moved. */ 
	protected long leftClicksMoved = 0;
	/** The number of right encoder clicks the robot has moved. */ 
	protected long rightClicksMoved = 0;
	/** If the robots speed has been reduced for final positioning or not. */
	protected boolean isSpeedReduced = false;
	/** If the PID algorithm is enabled or not. */
	protected boolean isPIDEnabled = true;

	/** Variables for the PID algorithm to maintain the error. */ 
	double integral = 0;
	double integralError = 0;							
	double leftError = 0;
	double rightError = 0;
	protected static final double INTEGRAL_ERROR_GAIN = 0.1;
	protected static final double PROPORTIONAL_GAIN = 0.1;
	
	/**
	 * Constructor
	 * 
	 */
	public MotorControlService (RobotIOIOInterface ioio) {
		this.ioio = ioio;
		
		//
		//Initialize a thread to perform our service activities
		//
		if (motorMonitorThread != null) {
			motorMonitorThread.stop();
		}
		motorMonitorThread = new Thread(new MotorMonitorJob(this));
		motorMonitorThread.setDaemon(true);
		motorMonitorThread.start();
	}

	/**
	 * Code which monitors the motors and stops them when the desired distance is reached,
	 * and also applies the PID algorithm to keep the robot on course.
	 *
	 */
	public class MotorMonitorJob implements Runnable {
		MotorControlService motorControlService;
		
		/**
		 * Constructor
		 * 
		 * @param motorControlService
		 */
		public MotorMonitorJob (MotorControlService motorControlService) {
			this.motorControlService = motorControlService;			
		}
		
		/**
		 * Thread runnable method
		 * 
		 */
		@Override
		public void run() {
			try {	
				for (;;) {
					if (ioio != null && ioio.isIOIOConnected() && motorControlService.isRobotMoving()) {
						//Log.d(LOGTAG,  "******************************");

						//
						//Get our encoder values
						//Because this is a quadrature encoder, we add together both encoders clicks
						//
						long sampledRightClicks = ioio.getRightMotorEncoder1().getPulseCount() + ioio.getRightMotorEncoder2().getPulseCount();
						long sampledLeftClicks = ioio.getLeftMotorEncoder1().getPulseCount() + ioio.getLeftMotorEncoder2().getPulseCount();

						motorControlService.clearMotorEncoderPulseCounts();
						
						//Log.d(LOGTAG,  "MotorControlService.MotorMonitorJob     Sampled Left Clicks: " + sampledLeftClicks + " Sampled Right Clicks: " + sampledRightClicks);
								
						//
						//Store total distance traveled
						//
						leftClicksMoved += sampledLeftClicks;
						rightClicksMoved += sampledRightClicks;

						Log.d(LOGTAG,  "MotorControlService.MotorMonitorJob      Left Clicks Moved: " + leftClicksMoved + " Right Clicks Moved: " + rightClicksMoved);
						Log.d(LOGTAG,  "MotorControlService.MotorMonitorJob      Left Clicks To Move: " + leftClicksToMove + " Right Clicks To Move: " + rightClicksToMove);
						
						//
						//Apply the PID algorithm and adjust the motors
						//
						if (isPIDEnabled) {
							integral = motorControlService.limitRange(((integral + sampledLeftClicks - sampledRightClicks)), -1000, 1000);							
							integralError = INTEGRAL_ERROR_GAIN * integral;							

							//Log.d(LOGTAG,  "MotorControlService.MotorMonitorJob     Integral: " + integral + " Integral Error: " + integralError);							
							//Log.d(LOGTAG,  "MotorControlService.MotorMonitorJob     desiredVelocityClicks: " + desiredVelocityClicks + " desiredBiasClicks: " + desiredBiasClicks);
 
							leftError = PROPORTIONAL_GAIN * (desiredVelocityClicks + desiredBiasClicks - sampledLeftClicks - integralError);
							rightError = PROPORTIONAL_GAIN * (desiredVelocityClicks + desiredBiasClicks - sampledRightClicks + integralError);
							
							
							Log.d(LOGTAG,  "MotorControlService.MotorMonitorJob     Left Velocity: " + motorControlService.getLeftMotorVelocity() + " Left Error: " + leftError);
							Log.d(LOGTAG,  "MotorControlService.MotorMonitorJob     Right Velocity: " + motorControlService.getRightMotorVelocity() + " Right Error: " + rightError);
							
							
						    //Take (Max motor Speed * ((Motor Speed Converted To %) + Error) ) / 100
						    //Adjust the left motor Speed
							//What percent of max speed is it currently going. Add on the error, and then set that
							//as the new percent of max speed
							double newLeftVelocityPct = (motorControlService.getLeftMotorVelocity() * 100) / MAX_MOTOR_SPEED;
							newLeftVelocityPct += leftError;
							double newLeftVelocity = (MAX_MOTOR_SPEED * newLeftVelocityPct) / 100;
							//Log.d(LOGTAG,  "MotorControlService.MotorMonitorJob     New Left Velocity Pct: " + newLeftVelocityPct + " New Left Velocity: " + newLeftVelocity);
							
							//Enable after we debug
							//This should be a value between 0 and 100
							motorControlService.setLeftMotorVelocity(newLeftVelocity);

							
						    //Adjust the right motor Speed
							double newRightVelocityPct = (motorControlService.getRightMotorVelocity() * 100) / MAX_MOTOR_SPEED;
							newRightVelocityPct += rightError;
							double newRightVelocity = (MAX_MOTOR_SPEED * newRightVelocityPct) / 100;
							//Log.d(LOGTAG,  "MotorControlService.MotorMonitorJob     New Right Velocity Pct: " + newRightVelocityPct + " New Right Velocity: " + newRightVelocity);

							//Enable after we debug
							//This should be a value between 0 and 100
							motorControlService.setRightMotorVelocity(newRightVelocity);
							
							/*
							int newRightVelocity = (motorControlService.getRightMotorVelocity() * 100) / MAX_MOTOR_SPEED;
							newRightVelocity = motorControlService.limitRange( (int) (newRightVelocity + rightError), 0, 100);
							newRightVelocity = (MAX_MOTOR_SPEED * newRightVelocity) / 100;
							motorControlService.setRightMotorVelocity(newRightVelocity);
							*/
						}
						
						//
						//Monitor the encoders for the distance traveled to see if we need to stop the robot
						//
						//Left encoder clicks
						if (leftClicksToMove > 0) {
							if (leftClicksToMove > 0  && leftClicksMoved >= leftClicksToMove) {
								motorControlService.stopMotors();
								//motorControlService.setLeftMotorVelocity(0);														
							}
							else if (!isSpeedReduced && leftClicksMoved >= (leftClicksToMove - 100)) {
								motorControlService.slowMotorsForFinalPositioning();
							}
						}
	
						//Right encoder clicks
						if (rightClicksToMove > 0) {
							if (rightClicksToMove > 0  && rightClicksMoved >= rightClicksToMove) {
								motorControlService.stopMotors();
								//motorControlService.setRightMotorVelocity(0);														
							}
							else if (!isSpeedReduced && rightClicksMoved >= (rightClicksToMove - 100)) {
								motorControlService.slowMotorsForFinalPositioning();
							}
						}	
												
						//
						//Stop motors once we reach the destination
						//
						if (leftClicksToMove > 0  && leftClicksMoved >= leftClicksToMove &&
							rightClicksToMove > 0  && rightClicksMoved >= rightClicksToMove) {
							motorControlService.stopMotors();
						}
												
					}
					
					//Sleep
					try {
						Thread.sleep(MOTOR_MONITOR_THREAD_SLEEP_MS);
					} catch (Exception e) {
					}
				}					
			} catch (Exception e) {
				Log.e(LOGTAG, "An error occurred in the MotorMonitorJob. " + e.toString(), e);
			}
		}		
	}
	
	
	/**
	 * Stops the motors and sets the brakes to enabled.
	 */
	public void stopMotors() throws ConnectionLostException {
		_isRobotMoving = false;		
		setBrakesEnabled(true);		
	}
	
	/**
	 * Enable or disable the brakes on the motors.
	 * @param areBrakesEnabled
	 */
	public void setBrakesEnabled(boolean areBrakesEnabled) throws ConnectionLostException {
		if (ioio.getLeftMotorBrake() != null) {
			ioio.getLeftMotorBrake().write(areBrakesEnabled);
		}
		if (ioio.getRightMotorBrake() != null) {
			ioio.getRightMotorBrake().write(areBrakesEnabled);
		}
		
		//
		//If we enable brakes, also stop the motors.
		//
		if (areBrakesEnabled) {
			_isRobotMoving = false;	
			setLeftMotorVelocity(0);
			setRightMotorVelocity(0);				        
		}		
 	}
	
	/**
	 * Turn the robot the number of degrees at the specified velocity.
	 * Velocity comes in as percentages from -100 to 100
	 * 
	 */
	public void turn (double degreesToTurn, int velocity) throws ConnectionLostException {
		Log.i(LOGTAG,  "MotorControlService.turn called. Degrees to Turn: " + degreesToTurn + " Velocity: " + velocity);
		
		//
		//Calculate the desired shaft encoder clicks to move
	    //Robot travels 1 degree per 4.701 encoder clicks, DesiredClicks = Degrees * 4.701
		//		
		rightClicksToMove = (int) (degreesToTurn * ENCODER_CLICKS_PER_TURN_DEGREE);		
		leftClicksToMove = rightClicksToMove;
		
		Log.i(LOGTAG,  "MotorControlService.turn called. Left Clicks to Move: " + leftClicksToMove + " Right Clicks to Move: " + rightClicksToMove);
		
		//
		//Start the robot turning
		//
	    this.move (0, velocity);		
	}
	
	/**
	 * Move the robot forwards or backwards the desired distance
	 * at the specified velocity.
	 * Velocity comes in as percentages from -100 to 100
	 * 
	 */
	public void moveDistance (double distanceToMoveCM, int velocity) throws ConnectionLostException {
		//
		//Calculate the desired shaft encoder clicks to move
	    //Robot travels .0459 centimeters per shaft click
		//
		rightClicksToMove = (int) (distanceToMoveCM / ENCODER_CLICKS_PER_CM);
		leftClicksToMove = rightClicksToMove;
		
		//
		//Start the robot turning
		//
	    this.move (velocity, 0);			
	}
	
	/**
	 * Move the robot.
	 * Velocity and Bias come in as percentages from -100 to 100
	 * The velocity is the velocity of both motors in percentage speed of motor max speed
	 * The bias is the rotational component, which will arc the robot
	 * 
	 */
	public void move (int velocity, int bias) throws ConnectionLostException {
		
		if (!ioio.isIOIOConnected()) {
			Log.e(LOGTAG, "Unable to move. The IOIO is not yet connected and initialized.");
		}
		else {
			Log.i(LOGTAG, "MotorControlService.move Velocity: " + velocity + " Bias: " + bias);
		}
				
		//
		//Ensure the parameters are within acceptable ranges
		//
		velocity = (int) limitRange(velocity, -100, 100);
		bias = (int) limitRange(bias, -100, 100);
		
		Log.d(LOGTAG, "MotorControlService.move After limit range applied to velocity and bias. Velocity: " + velocity + " Bias: " + bias);
		
        //
		//Calculate the encoder clicks per time interval we want to achieve
		//
        desiredVelocityClicks = (MAX_SPEED_PER_INTERVAL * Math.abs(velocity)) / 100;
        desiredBiasClicks = (MAX_SPEED_PER_INTERVAL * Math.abs(bias)) / 100;

        Log.d(LOGTAG, "MotorControlService.move desiredVelocityClicks: " + desiredVelocityClicks + " desiredBiasClicks: " + desiredBiasClicks);
                
        //
        //Clear out all tracking variables for encoder counts and distance traveled
        //
        resetMotorEncoderTrackingVariables();
        clearMotorEncoderPulseCounts();
        
        //
        //Set our intial Velocities
        //        
        leftMotorVelocity = limitRange (((MAX_MOTOR_SPEED * Math.abs((velocity + bias)))/100), 0, MAX_MOTOR_SPEED);
        setLeftMotorVelocity(leftMotorVelocity);
        
        rightMotorVelocity = limitRange (((MAX_MOTOR_SPEED * Math.abs((velocity - bias)))/100), 0, MAX_MOTOR_SPEED);
        setRightMotorVelocity(rightMotorVelocity);
        
        //
        //Set the direction of the motors to turn
        //
        //Left motor
        boolean isLeftMotorGoingForward = ((velocity + bias) >= 0);
        ioio.getLeftMotorDirection().write(isLeftMotorGoingForward);
        boolean isRightMotorGoingForward = ((velocity - bias) >= 0);
        ioio.getRightMotorDirection().write(isRightMotorGoingForward);
		
        //
		//Ensure the brakes are disabled
        //
        this.setBrakesEnabled(false);
        
		if (velocity != 0 || bias != 0) {
			_isRobotMoving = true;
		}        
	}
	
	/**
	 * Clear the encoder pulse counts.
	 * 
	 * @throws ConnectionLostException
	 */
	public void clearMotorEncoderPulseCounts () throws ConnectionLostException {
		ioio.getLeftMotorEncoder1().clearPulseCount();
		ioio.getLeftMotorEncoder2().clearPulseCount();
		ioio.getRightMotorEncoder1().clearPulseCount();
		ioio.getRightMotorEncoder2().clearPulseCount();
	}
	
	/**
	 * Reset the variables used by the motor monitor thread to control
	 * the motors and ensure the robot moves as expected.
	 * 
	 */
	public void resetMotorEncoderTrackingVariables() {
		leftClicksMoved = 0;
		rightClicksMoved = 0;
		isSpeedReduced = false;
		isPIDEnabled = true;
		
		integral = 0;
		integralError = 0;							
		leftError = 0;
		rightError = 0;
		
	}
	
	/**
	 * Limits a number passed in so it is not greater than the HighLimitVal parameter
	 * or less than the LowLimitVal Parameter
	 * 
	 * @param actualValue
	 * @param lowLimitValue
	 * @param highLimitValue
	 * @return
	 */
	public double limitRange (double actualValue, double lowLimitValue, double highLimitValue) {
		if (actualValue > highLimitValue) {
			return highLimitValue;
		}
		else if (actualValue < lowLimitValue) {
			return lowLimitValue;
		}
		else {
			return actualValue;
		}
	}
	
	/**
	 * Set the velocity to move the left motor at, between 0 and 100
	 * 
	 * @throws ConnectionLostException
	 */
	public void setLeftMotorVelocity (double motorVelocity) throws ConnectionLostException {
		//Log.i(LOGTAG, "MotorControlService.setLeftMotorVelocity motorVelocity: " + motorVelocity);
		motorVelocity = limitRange (motorVelocity, 0, MAX_MOTOR_SPEED);
		if (ioio.getLeftMotorPwm() != null) {
			this.leftMotorVelocity = motorVelocity;
	        float dutyCycle = (float) leftMotorVelocity / 100.00f;        
	        
	        //Log.i(LOGTAG, "MotorControlService.setRightMotorVelocity set duty cycle to: " + dutyCycle);
	        
			ioio.getLeftMotorPwm().setDutyCycle(dutyCycle);
		}
	}	

	/**
	 * Set the velocity to move the right motor at, between 0 and 100
	 * 
	 * @throws ConnectionLostException
	 */
	public void setRightMotorVelocity (double motorVelocity) throws ConnectionLostException {
		//Log.i(LOGTAG, "MotorControlService.setRightMotorVelocity motorVelocity: " + motorVelocity);
		motorVelocity = limitRange (motorVelocity, 0, MAX_MOTOR_SPEED);
		if (ioio.getRightMotorPwm() != null) {
			this.rightMotorVelocity = motorVelocity;
	        float dutyCycle = (float) rightMotorVelocity / 100.00f;
	        
	        //Log.i(LOGTAG, "MotorControlService.setRightMotorVelocity set duty cycle to: " + dutyCycle);
	        
			ioio.getRightMotorPwm().setDutyCycle(dutyCycle);
		}
	}
	
	/**
	 * Get the velocity set for the left motor.
	 * @return
	 */
	public double getLeftMotorVelocity() {
		return leftMotorVelocity;
	}

	/**
	 * Get the velocity set for the right motor.
	 * @return
	 */
	public double getRightMotorVelocity() {
		return rightMotorVelocity;
	}
	
	/**
	 * This method will slow both the motors down to the predefined
	 * SLOW_MOTOR_POSITIONING_SPEED if the speed of the motor is greater than
	 * that speed.
	 * It also sets the flag that speed has been reduced and disables PID on the
	 * motors to avoid speeding them back up.
	 * 
	 */
	public void slowMotorsForFinalPositioning() throws ConnectionLostException {
		isSpeedReduced = true;
		isPIDEnabled = false;
		
		if (getLeftMotorVelocity() > SLOW_MOTOR_POSITIONING_SPEED) {
			setLeftMotorVelocity(SLOW_MOTOR_POSITIONING_SPEED);
		}
		if (getRightMotorVelocity() > SLOW_MOTOR_POSITIONING_SPEED) {
			setRightMotorVelocity(SLOW_MOTOR_POSITIONING_SPEED);
		}
	}
	
	/** 
	 * Return whether or not the the robot is currently moving. 
	 * 
	 */
	public boolean isRobotMoving() {
		return _isRobotMoving;
	}
	
}
