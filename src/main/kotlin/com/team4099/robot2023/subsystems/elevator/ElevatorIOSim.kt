package com.team4099.robot2023.subsystems.elevator

import com.team4099.robot2023.config.constants.Constants
import com.team4099.robot2023.config.constants.ElevatorConstants
import com.team4099.robot2023.util.ElevatorSim
import edu.wpi.first.math.MathUtil
import edu.wpi.first.math.system.plant.DCMotor
import edu.wpi.first.wpilibj.simulation.BatterySim
import edu.wpi.first.wpilibj.simulation.RoboRioSim
import org.team4099.lib.controller.PIDController
import org.team4099.lib.units.base.Length
import org.team4099.lib.units.base.Meter
import org.team4099.lib.units.base.amps
import org.team4099.lib.units.base.celsius
import org.team4099.lib.units.base.inSeconds
import org.team4099.lib.units.base.meters
import org.team4099.lib.units.derived.DerivativeGain
import org.team4099.lib.units.derived.ElectricalPotential
import org.team4099.lib.units.derived.IntegralGain
import org.team4099.lib.units.derived.ProportionalGain
import org.team4099.lib.units.derived.Volt
import org.team4099.lib.units.derived.inVolts
import org.team4099.lib.units.derived.volts
import org.team4099.lib.units.perSecond

object ElevatorIOSim : ElevatorIO {
  val elevatorSim: ElevatorSim =
    ElevatorSim(
      DCMotor.getNEO(2),
      ElevatorConstants.GEAR_RATIO,
      ElevatorConstants.CARRIAGE_MASS,
      ElevatorConstants.SPOOL_RADIUS,
      ElevatorConstants.ELEVATOR_MAX_RETRACTION,
      ElevatorConstants.ELEVATOR_MAX_EXTENSION,
      ElevatorConstants.ELEVATOR_ANGLE,
      true,
    )

  private val elevatorController =
    PIDController(ElevatorConstants.SIM_KP, ElevatorConstants.SIM_KI, ElevatorConstants.SIM_KD)

  override fun updateInputs(inputs: ElevatorIO.ElevatorInputs) {
    elevatorSim.update(Constants.Universal.LOOP_PERIOD_TIME.inSeconds)

    inputs.elevatorPosition = elevatorSim.positionMeters.meters
    inputs.elevatorVelocity = elevatorSim.velocityMetersPerSecond.meters.perSecond

    inputs.leaderTempCelcius = 0.0.celsius
    inputs.leaderStatorCurrent = 0.0.amps
    inputs.leaderSupplyCurrent = elevatorSim.currentDrawAmps.amps / 2
    inputs.leaderAppliedVoltage = 0.0.volts

    inputs.followerTempCelcius = 0.0.celsius
    inputs.followerStatorCurrent = 0.0.amps
    inputs.followerSupplyCurrent = elevatorSim.currentDrawAmps.amps / 2
    inputs.followerAppliedVoltage = 0.0.volts

    RoboRioSim.setVInVoltage(
      BatterySim.calculateDefaultBatteryLoadedVoltage(elevatorSim.currentDrawAmps)
    )
  }

  /**
   * Sets the voltage of the elevator motors but also checks to make sure elevator doesn't exceed
   * limit
   *
   * @param voltage the voltage to set the motor to
   */
  override fun setOutputVoltage(voltage: ElectricalPotential) {
    // divide by 2 cause full power elevator is scary
    elevatorSim.setInputVoltage(
      MathUtil.clamp(
        voltage.inVolts,
        -ElevatorConstants.VOLTAGE_COMPENSATION.inVolts / 2,
        ElevatorConstants.VOLTAGE_COMPENSATION.inVolts / 2
      )
    )
  }

  /**
   * Sets the voltage of the elevator motors but also checks to make sure elevator doesn't exceed
   * limit
   *
   * @param position the target position the PID controller will use
   * @param feedforward change in voltage to account for external forces on the system
   */
  override fun setPosition(position: Length, feedforward: ElectricalPotential) {
    val ff =
      MathUtil.clamp(
        feedforward.inVolts,
        -ElevatorConstants.VOLTAGE_COMPENSATION.inVolts,
        ElevatorConstants.VOLTAGE_COMPENSATION.inVolts
      )
        .volts
    val feedback = elevatorController.calculate(elevatorSim.positionMeters.meters, position)
    elevatorSim.setInputVoltage((ff + feedback).inVolts)
  }

  /** set the current encoder position to be the encoders zero value */
  override fun zeroEncoder() {
    println("don't work right now")
  }

  /**
   * updates the PID controller values using the sensor measurement for proportional intregral and
   * derivative gain multiplied by the 3 PID constants
   *
   * @param kP a constant which will be used to scale the proportion gain
   * @param kI a constant which will be used to scale the integral gain
   * @param kD a constant which will be used to scale the derivative gain
   */
  override fun configPID(
    kP: ProportionalGain<Meter, Volt>,
    kI: IntegralGain<Meter, Volt>,
    kD: DerivativeGain<Meter, Volt>
  ) {
    elevatorController.setPID(kP, kI, kD)
  }
}