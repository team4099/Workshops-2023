package com.team4099.robot2023

import com.team4099.robot2023.auto.AutonomousSelector
import com.team4099.robot2023.commands.drivetrain.ResetGyroYawCommand
import com.team4099.robot2023.commands.drivetrain.TeleopDriveCommand
import com.team4099.robot2023.config.ControlBoard
import com.team4099.robot2023.config.constants.Constants
import com.team4099.robot2023.subsystems.drivetrain.drive.Drivetrain
import com.team4099.robot2023.subsystems.drivetrain.drive.DrivetrainIOReal
import com.team4099.robot2023.subsystems.drivetrain.drive.DrivetrainIOSim
import com.team4099.robot2023.subsystems.drivetrain.gyro.GyroIO
import com.team4099.robot2023.subsystems.drivetrain.gyro.GyroIONavx
import com.team4099.robot2023.subsystems.elevator.Elevator
import com.team4099.robot2023.subsystems.elevator.ElevatorIONeo
import com.team4099.robot2023.subsystems.elevator.ElevatorIOSim
import com.team4099.robot2023.subsystems.groundintake.GroundIntake
import com.team4099.robot2023.subsystems.groundintake.GroundIntakeIONeo
import com.team4099.robot2023.subsystems.groundintake.GroundIntakeIOSim
import com.team4099.robot2023.subsystems.manipulator.Manipulator
import com.team4099.robot2023.subsystems.manipulator.ManipulatorIONeo
import com.team4099.robot2023.subsystems.manipulator.ManipulatorIOSim
import com.team4099.robot2023.subsystems.superstructure.Superstructure
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj2.command.InstantCommand
import org.team4099.lib.smoothDeadband

object RobotContainer {
  private val drivetrain: Drivetrain
  private val superstructure: Superstructure

  init {
    if (RobotBase.isReal()) {
      // Real Hardware Implementations
      drivetrain = Drivetrain(GyroIONavx, DrivetrainIOReal)
      superstructure =
        Superstructure(
          Elevator(ElevatorIONeo),
          GroundIntake(GroundIntakeIONeo),
          Manipulator(ManipulatorIONeo)
        )
    } else {
      // Simulation implementations
      drivetrain = Drivetrain(object : GyroIO {}, DrivetrainIOSim)
      superstructure =
        Superstructure(
          Elevator(ElevatorIOSim),
          GroundIntake(GroundIntakeIOSim),
          Manipulator(ManipulatorIOSim)
        )
    }
  }

  fun mapDefaultCommands() {
    drivetrain.defaultCommand =
      TeleopDriveCommand(
        { ControlBoard.strafe.smoothDeadband(Constants.Joysticks.THROTTLE_DEADBAND) },
        { ControlBoard.forward.smoothDeadband(Constants.Joysticks.THROTTLE_DEADBAND) },
        { ControlBoard.turn.smoothDeadband(Constants.Joysticks.TURN_DEADBAND) },
        { ControlBoard.robotOriented },
        drivetrain
      )
    superstructure.defaultCommand = InstantCommand({}, superstructure)
  }

  fun zeroSteering() {
    drivetrain.zeroGyroYaw()
  }

  fun zeroSensors() {
    drivetrain.zeroSensors()
  }

  fun setDriveCoastMode() {
    drivetrain.swerveModules.forEach { it.setDriveBrakeMode(false) }
  }

  fun setDriveBrakeMode() {
    drivetrain.swerveModules.forEach { it.setDriveBrakeMode(true) }
  }

  fun mapTeleopControls() {
    ControlBoard.resetGyro.whileActiveOnce(ResetGyroYawCommand(drivetrain))
    //
    // ControlBoard.advanceAndClimb.whileActiveOnce(AdvanceClimberCommand().andThen(RunClimbCommand()))
    //        ControlBoard.climbWithoutAdvance.whileActiveOnce(RunClimbCommand())
    //    ControlBoard.extendArm.whileTrue(manipulator.openLoopControl(12.0.volts))
    //    ControlBoard.retractArm.whileTrue(manipulator.openLoopControl(-12.0.volts))

    ControlBoard.setArmPositionToShelfIntake.whileTrue(superstructure.prepscoreConeHighCommand())
    ControlBoard.extendArm.whileTrue(superstructure.scoreConeHighCommand())

    //
    // ControlBoard.setArmPositionToShelfIntake.whileTrue(superstructure.elevatorGoToHighConeNodeCommand())
    //    ControlBoard.extendArm.whileTrue(superstructure.groundIntakeIntakeCommand())
    //    ControlBoard.retractArm.whileTrue(superstructure.manipulatorGoToMaxExtensionCommand())

    /*
    ControlBoard.intakeCone.whileTrue(
      manipulator.manipulatorCommand(
        ManipulatorConstants.RollerStates.CONE_IN,
        ManipulatorConstants.ArmStates.MIN_EXTENSION)
    )
    ControlBoard.intakeCube.whileTrue(
      manipulator.manipulatorCommand(
        ManipulatorConstants.RollerStates.CUBE_IN,
        ManipulatorConstants.ArmStates.MIN_EXTENSION)
    )
    ControlBoard.outtakeCone.whileTrue(
      manipulator.manipulatorCommand(
        ManipulatorConstants.RollerStates.CONE_OUT,
        ManipulatorConstants.ArmStates.MIN_EXTENSION
      )
    )
    ControlBoard.outtakeCube.whileTrue(
      manipulator.manipulatorCommand(
        ManipulatorConstants.RollerStates.CUBE_OUT,
        ManipulatorConstants.ArmStates.MIN_EXTENSION
      )
    )
     */

    //    ControlBoard.runElevatorToHighNode.whileTrue(elevator.goToHighConeNodeCommand())
    //
    //    ControlBoard.openLoopExtend.whileTrue(elevator.openLoopExtendCommand())
    //    ControlBoard.openLoopRetract.whileTrue(elevator.openLoopRetractCommand())
    //
    //    ControlBoard.extendIntake.whileTrue(groundIntake.intakeCommand())
    //    ControlBoard.retractIntake.whileTrue(groundIntake.stowedUpCommand())
    //    //    ControlBoard.characterizeIntake.whileTrue(
    //    //
    // groundIntake.groundIntakeDeployCommand(GroundIntakeConstants.ArmStates.TUNABLE_STATE) //
    //    // TODO make legit
    //    //    )
    //
    //    ControlBoard.setArmCommand.whileTrue(groundIntake.stowedDownCommand())
  }

  fun mapTestControls() {}

  //  fun getAutonomousCommand() =
  //    AutonomousSelector.getCommand(
  //      drivetrain, intake, feeder, shooter, telescopingClimber, pivotClimber
  //    )"

  fun getAutonomousCommand() = AutonomousSelector.getCommand(drivetrain)

  fun mapTunableCommands() {
    //    val commandsTab = Shuffleboard.getTab("TunableCommands")
    //    commandsTab.add(manipulator)
    //    SendableRegistry.setName(manipulator, "manipulator")
    //    commandsTab.add("ManipulatorArmCharacterization", ArmCharacterizationCommand(manipulator))
    //    commandsTab.add(
    //      "ManipulatorArmTuning",
    //      manipulator.scoreConeAtHighNodeCommand( // TODO fix
    //      )
    //    )
    //    commandsTab.add(RobotContainer.elevator)
    //    SendableRegistry.setName(RobotContainer.elevator, "elevator")
    //    commandsTab.add("ElevatorCharacterization", ElevatorCharacterizeCommand(elevator))
    //    commandsTab.add(
    //      "ElevatorTuning", elevator.goToMidConeNodeCommand() // TODO FIX
    //    )
    //    commandsTab.add(groundIntake)
    //    SendableRegistry.setName(groundIntake, "groundIntake")
    //    commandsTab.add(
    //      "GroundIntakeArmCharacterization", GroundIntakeCharacterizeCommand(groundIntake)
    //    )
    //    commandsTab.add("GroundIntakeArmTuning", groundIntake.stowedUpCommand())
  }
}
