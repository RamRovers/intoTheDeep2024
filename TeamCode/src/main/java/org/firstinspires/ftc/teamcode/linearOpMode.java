package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;

@TeleOp(name = "RambotsPurpleTeleOp")
public class linearOpMode extends LinearOpMode {
  private DcMotor frontLeftMotor = null, backLeftMotor = null;
  private DcMotor frontRightMotor = null, backRightMotor = null;
  private DcMotor slideExtension = null;
  private DcMotor slideAbduction = null;
  private DcMotor slideAbduction2 = null;
  private Servo  wrist1 = null;
  private Servo  wrist2 = null;
  private CRServo leftIntake = null;
  private CRServo rightIntake = null;
  private double intakePower = 0;

  // THE SENSOR
  private ColorSensor sensor  = null;

  @Override
  public void runOpMode() {

    // initializing hardware

    // Drive Train motor
    frontLeftMotor = hardwareMap.get(DcMotor.class, "leftFront");
    frontRightMotor = hardwareMap.get(DcMotor.class, "rightFront");
    backLeftMotor = hardwareMap.get(DcMotor.class, "leftBack");
    backRightMotor = hardwareMap.get(DcMotor.class, "rightBack");

    // DcMotors for Linear slide
    slideExtension = hardwareMap.get(DcMotor.class, "slideExtend");
    wrist1 = hardwareMap.get(Servo.class, "wrist1");
    wrist2 = hardwareMap.get(Servo.class, "wrist2");
    slideAbduction = hardwareMap.get(DcMotor.class, "slideAbd");
    slideAbduction2 = hardwareMap.get(DcMotor.class, "slideAbd2");

    slideExtension.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
    slideAbduction.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);
    slideAbduction2.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE);


    // Takers
    leftIntake = hardwareMap.get(CRServo.class, "l_intake");
    rightIntake = hardwareMap.get(CRServo.class, "r_intake");
    sensor = hardwareMap.get(ColorSensor.class, "sensor");

    // MaybeIntake = hardwareMap.get(DcMotor.class, "intake");
    // Setting the direction for the motor on where to rotate

    // Orientation for drivetrain
    frontLeftMotor.setDirection(DcMotor.Direction.FORWARD);
    frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
    backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
    backRightMotor.setDirection(DcMotor.Direction.REVERSE);

    // intake
    leftIntake.setDirection(CRServo.Direction.FORWARD);
    rightIntake.setDirection(CRServo.Direction.REVERSE);
    boolean intakeReleased = true;

    // linear slide
    slideExtension.setDirection(DcMotor.Direction.FORWARD);
    slideAbduction.setDirection(DcMotor.Direction.FORWARD);
    slideAbduction2.setDirection(DcMotor.Direction.REVERSE);

    //wrist
    waitForStart();

    if (isStopRequested()) return;

    while (opModeIsActive()) {

      /*
        GamePad Map
        GamePad 1 (Driver)
          Left JoyStick = lateral, diagonal, forwards and backwards movements
          Right JoyStick = Rotation of drive train
        GamePad 2 (Operator)
          Button A = toggle position of claw to open or closed (We start closed)
          left stick x = slide extension
          right stick y = slide abduction
       */

      // linear slide controls
      double slideExtendPower = gamepad2.left_stick_y;
      double slideAbdPower = gamepad2.right_stick_y;
      double wristpower = gamepad2.left_stick_x;

      //theory
      /*
       wristPower = gamepad2.left_trigger
       wristPower = -gamepad2.right_trigger

      */

      double wristPower = 0;
      if (gamepad2.right_trigger) {
        wristPower = 1;
      } else if (gamepad2.left_trigger > 0) {
        wristPower = -1;
      } else {
        wristPower = slideExtendPower;
      }

      //theory
      /*
       wristPower = gamepad2.left_trigger
       wristPower = -gamepad2.right_trigger

      */

      // drive train controls
      double y = -gamepad1.left_stick_y;
      double x = gamepad1.left_stick_x * 1.1;
      double turn = gamepad1.right_stick_x;

      // input: theta and power
      // theta is where we want the direction the robot to go
      // power is (-1) to 1 scale where increasing power will cause the engines to go faster
      double theta = Math.atan2(y, x);
      double power = Math.hypot(x, y);
      double sin = Math.sin(theta - Math.PI / 4);
      double cos = Math.cos(theta - Math.PI / 4);
      // max variable allows to use the motors at its max power with out disabling it
      double max = Math.max(Math.abs(sin), Math.abs(cos));

      double frontLeftPower = power * cos / max + turn;
      double frontRightPower = power * cos / max - turn;
      double backLeftPower = power * sin / max + turn;
      double backRightPower = power * sin / max - turn;

      // Prevents the motors exceeding max power thus motors will not seize and act sporadically
      if ((power + Math.abs(turn)) > 1) {
        frontLeftPower /= power + turn;
        frontRightPower /= power - turn;
        backLeftPower /= power + turn;
        backRightPower /= power - turn;
      }

      // if A on the controller is pressed it will check if the claw is closed
      // HOTFIX: A is open, B is close
      if (gamepad2.a && intakeReleased) {
        intakePower = (intakePower == 1 ? 0 : 1);
        intakeReleased = false;
      } else if (gamepad2.b && intakeReleased) {
        intakePower = (intakePower == -1 ? 0 : -1);
        intakeReleased = false;
      }

      if(!gamepad2.a && !gamepad2.b) {
        intakeReleased = true;
      }




      // Power to the wheels
      frontLeftMotor.setPower(frontLeftPower);
      backLeftMotor.setPower(backLeftPower);
      frontRightMotor.setPower(frontRightPower);
      backRightMotor.setPower(backRightPower);

      // Power to the arm
      slideAbduction.setPower(slideAbdPower);
      slideAbduction2.setPower(slideAbdPower);
      slideExtension.setPower(slideExtendPower);

      // Wrist power
      wrist1.setPosition(wristpower);
      wrist2.setPosition(wristpower);
      // Power to the intake
      leftIntake.setPower(intakePower);
      rightIntake.setPower(intakePower);

      // Telemetry
      telemetry.addData("X", x);
      telemetry.addData("Y", y);
      telemetry.addData("Alpha", sensor.alpha());
      telemetry.addData("Red  ", sensor.red());
      telemetry.addData("Green", sensor.green());
      telemetry.addData("Blue ", sensor.blue());
      telemetry.addData("Intake power: ", intakePower);
      telemetry.addData("Slide extension power: ", slideExtendPower);
      telemetry.addData("Slide abduction power: ", slideAbdPower);
      telemetry.addData("Wrist power: ", wristpower);
      telemetry.update();

    }
  }
}