package com.timarcher.robotcontrolsystemng.robot.ioio;

/**
 * This class simply provides the constants for which functions 
 * are mapped to which function on the IOIO board.
 *  
 * @author tarcher
 *
 */
public class RobotIOIOPinConstants {
	/** Pin constants / declarations */
	//See pin capabilities on https://github.com/ytai/ioio/wiki/Getting-To-Know-The-Board
	public static int PIN_I2C_SDA0						= 4;
	public static int PIN_I2C_SCL0						= 5;
	
	public static int PIN_LEFT_MOTOR_PWM 				= 6;
	public static int PIN_RIGHT_MOTOR_PWM 				= 7;
	
	public static int PIN_LEFT_MOTOR_DIRECTION 			= 8;
	public static int PIN_RIGHT_MOTOR_DIRECTION 		= 9;
	
	public static int PIN_LEFT_MOTOR_BRAKE 				= 10;
	public static int PIN_RIGHT_MOTOR_BRAKE 			= 11;
	
	public static int PIN_LEFT_MOTOR_THERMAL_FLAG 		= 12;
	public static int PIN_RIGHT_MOTOR_THERMAL_FLAG 		= 13;
	
	public static int PIN_LEFT_MOTOR_ENCODER_1 			= 14;
	public static int PIN_LEFT_MOTOR_ENCODER_2 			= 15;
	public static int PIN_RIGHT_MOTOR_ENCODER_1 		= 16;
	public static int PIN_RIGHT_MOTOR_ENCODER_2 		= 17;
		
	public static int PIN_LEFT_MOTOR_CURRENT 			= 31;
	public static int PIN_RIGHT_MOTOR_CURRENT 			= 32;
}
