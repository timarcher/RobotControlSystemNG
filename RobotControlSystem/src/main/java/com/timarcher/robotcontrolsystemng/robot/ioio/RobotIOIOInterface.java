package com.timarcher.robotcontrolsystemng.robot.ioio;

import android.util.Log;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

/**
 * This is the main IOIO looper class.
 * It provides the interface to the IOIO hardware device. Setup is called when a 
 * connection is made between the android os and the IOIO.
 * 
 * For this to work, the IOIOLibAndroid project was modified.
 * ioio.lib.impl.DigitalInputImpl and ioio.lib.api.DigitalInput were
 * both modified to count pulses and expose a getPulseCount and clearPulseCount methods.
 * 
 */
public class RobotIOIOInterface extends BaseIOIOLooper {
	/** Logging tag. */
	protected static final String LOGTAG = "RobotIOIOInterface";
	
	/** Whether the IOIO device is currently connected. */
	protected boolean _isIOIOConnected = false;
	/** Counter for how many times the loop has been executed. */
	protected int ioioLoopCount = 0;
	
	/** The onboard LED on the IOIO board. */
	protected DigitalOutput onboardLed;

	/** Left motor control pins. */
	protected PwmOutput leftMotorPwm;
	protected DigitalOutput leftMotorDirection;
	protected DigitalOutput leftMotorBrake;
	protected DigitalInput leftMotorThermalFlag;
	protected AnalogInput leftMotorCurrent;
	
	/** Right motor control pins. */
	protected PwmOutput rightMotorPwm;
	protected DigitalOutput rightMotorDirection;
	protected DigitalOutput rightMotorBrake;
	protected DigitalInput rightMotorThermalFlag;
	protected AnalogInput rightMotorCurrent;

	/** Left motor quadrature encoder */
	protected DigitalInput leftMotorEncoder1;		
	protected DigitalInput leftMotorEncoder2;		
	
	/** Right motor quadrature encoder */
	protected DigitalInput rightMotorEncoder1;		
	protected DigitalInput rightMotorEncoder2;		

	
	/**
	 * Called when the Android OS is successfully connected to the 
	 * IOIO.
	 * 
	 */
	@Override
	protected void setup() throws ConnectionLostException, InterruptedException {
		Log.i(LOGTAG,  "RobotIOIOInterface.setup called");
		
		onboardLed = ioio_.openDigitalOutput(IOIO.LED_PIN);
		
		//
		//Configure the pins which control the motors
		//
		//PWM Can be enabled on Pins 3-7, 10-14, 18-26, and 47-48
		//Borrowed some example code from here: http://robotfreak.googlecode.com/svn/trunk/ioio/IOIORobotController/src/ioio/examples/robot_controller/IOIORobotControllerActivity.java
		//1000 seems to be the fastest speed/frequency for controlling the motor speed
		//
		leftMotorPwm = ioio_.openPwmOutput(RobotIOIOPinConstants.PIN_LEFT_MOTOR_PWM, 1000);  //pin, freq
		leftMotorDirection = ioio_.openDigitalOutput(RobotIOIOPinConstants.PIN_LEFT_MOTOR_DIRECTION);
		leftMotorBrake = ioio_.openDigitalOutput(RobotIOIOPinConstants.PIN_LEFT_MOTOR_BRAKE);
		leftMotorThermalFlag = ioio_.openDigitalInput(RobotIOIOPinConstants.PIN_LEFT_MOTOR_THERMAL_FLAG);
		leftMotorCurrent = ioio_.openAnalogInput(RobotIOIOPinConstants.PIN_LEFT_MOTOR_CURRENT);
		leftMotorEncoder1 = ioio_.openDigitalInput(RobotIOIOPinConstants.PIN_LEFT_MOTOR_ENCODER_1);
		leftMotorEncoder2 = ioio_.openDigitalInput(RobotIOIOPinConstants.PIN_LEFT_MOTOR_ENCODER_2);
		
		rightMotorPwm = ioio_.openPwmOutput(RobotIOIOPinConstants.PIN_RIGHT_MOTOR_PWM, 1000);  //pin, freq
		rightMotorDirection = ioio_.openDigitalOutput(RobotIOIOPinConstants.PIN_RIGHT_MOTOR_DIRECTION);
		rightMotorBrake = ioio_.openDigitalOutput(RobotIOIOPinConstants.PIN_RIGHT_MOTOR_BRAKE);
		rightMotorThermalFlag = ioio_.openDigitalInput(RobotIOIOPinConstants.PIN_RIGHT_MOTOR_THERMAL_FLAG);
		rightMotorCurrent = ioio_.openAnalogInput(RobotIOIOPinConstants.PIN_RIGHT_MOTOR_CURRENT);		
		rightMotorEncoder1 = ioio_.openDigitalInput(RobotIOIOPinConstants.PIN_RIGHT_MOTOR_ENCODER_1);
		rightMotorEncoder2 = ioio_.openDigitalInput(RobotIOIOPinConstants.PIN_RIGHT_MOTOR_ENCODER_2);

		//Set variable so other services will know the IOIO is connected
		ioioLoopCount = 0;	
		_isIOIOConnected = true;		
	}

	/**
	 * Called repeatedly from the Service when the android OS
	 * is connected to the IOIO. 
	 */
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		ioioLoopCount ++;
		
		//blink the onboard led
		onboardLed.write(false);
		Thread.sleep(500);
		onboardLed.write(true);
		Thread.sleep(500);
	}
	
	/**
	 * Called when the android OS is disconnected from the IOIO.
	 * 
	 */
	@Override
	public void disconnected() {
		_isIOIOConnected = false;
	}
	
	/**
	 * Return boolean true/false as to whether the Android OS
	 * is currently connected to the IOIO device.
	 * 
	 * @return
	 */
	public boolean isIOIOConnected() {
		return _isIOIOConnected;
	}
	
	/**
	 * Return the loop count of the IOIO device.
	 * 
	 * @return
	 */
	public int getLoopCount() {
		return ioioLoopCount;
	}

	/**
	 * Does a hard reset of the IOIO board.
	 * A hard reset is exactly like physically powering off the IOIO board and powering it back on. 
	 * As a result, the connection with the IOIO will drop and the IOIO instance will become disconnected and unusable. 
	 * The board will perform a full reboot, including going through the bootloader sequence.
	 * 
	 */
	public void hardReset() throws ConnectionLostException {
		ioio_.hardReset();
	}

	/**
	 * Does a soft reset of the IOIO board.
	 * A soft reset means "return everything to its initial state". This 
	 * includes closing all interfaces obtained from this IOIO instance, and in turn 
	 * freeing all resources. All pins (save the stat LED) will become floating inputs. 
	 * All modules will be powered off. These operations are done without dropping the 
	 * connection with the IOIO, thus a soft reset is very fast. It is generally not a good 
	 * practice to replace proper closing of sub-instances with a soft reset. However, under 
	 * some circumstances, such as tests, this might make sense.
	 * 
	 */
	public void softReset() throws ConnectionLostException {
		ioio_.softReset();
	}
	
	
	public DigitalOutput getOnboardLed() {
		return onboardLed;
	}

	public PwmOutput getLeftMotorPwm() {
		return leftMotorPwm;
	}

	public DigitalOutput getLeftMotorDirection() {
		return leftMotorDirection;
	}

	public DigitalOutput getLeftMotorBrake() {
		return leftMotorBrake;
	}

	public DigitalInput getLeftMotorThermalFlag() {
		return leftMotorThermalFlag;
	}

	public AnalogInput getLeftMotorCurrent() {
		return leftMotorCurrent;
	}

	public PwmOutput getRightMotorPwm() {
		return rightMotorPwm;
	}

	public DigitalOutput getRightMotorDirection() {
		return rightMotorDirection;
	}

	public DigitalOutput getRightMotorBrake() {
		return rightMotorBrake;
	}

	public DigitalInput getRightMotorThermalFlag() {
		return rightMotorThermalFlag;
	}

	public AnalogInput getRightMotorCurrent() {
		return rightMotorCurrent;
	}

	public DigitalInput getLeftMotorEncoder1() {
		return leftMotorEncoder1;
	}

	public DigitalInput getLeftMotorEncoder2() {
		return leftMotorEncoder2;
	}

	public DigitalInput getRightMotorEncoder1() {
		return rightMotorEncoder1;
	}

	public DigitalInput getRightMotorEncoder2() {
		return rightMotorEncoder2;
	}	
}
