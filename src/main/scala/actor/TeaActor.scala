package actor

import actor.CombineActor.{CombineActor, CombineException, CombineTeaMsg}
import actor.FrothMilkActor.FrothingException
import actor.GrassActor.{Grass, GrassActor, GrassDoneMsg, GrassMsg, GroundGrass}
import actor.GrindActor.GrindingException
import actor.HeatWaterActor.{HeatWaterActor, WaterBoilingException}
import actor.WaterStorageActor.{GetWaterAndHeatMsg, HeatWaterDoneMsg, Water, WaterLackException}
import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}
import scala.concurrent.duration.DurationInt

object TeaActor {

  case class TeaInit(grass: Grass, time: Long)

  case class TeaMsg(grass: Grass, time: Long)

  case class Tea(value: String)

  class TeaActor(coffeeMachine: ActorRef) extends Actor {
    private val grassActor = context.actorOf(Props(new GrassActor(self)), "GrassActor")
    private val heatWaterActor = context.actorOf(Props(new HeatWaterActor(self)), "HeatWaterActor")
    private val combineActor = context.actorOf(Props(new CombineActor(self)), "CombineActor")
    var water: Water = Water(0, 20)
    var groundGrass: Option[GroundGrass] = None
    var start: Long = 0

    override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case ae: GrindingException =>
        println(s"GrindingException -> Resume: [${ae.getMessage}]")
        Resume
      case we: WaterBoilingException =>
        println(s"WaterBoilingException -> Resume: [${we.getMessage}]")
        Resume
      case we: WaterLackException =>
        println(s"WaterLackException -> Resume HeatWaterActor: [${we.getMessage}]")
        Thread.sleep(1000)
        heatWater()
        Resume
      case ce: CombineException =>
        println(s"CombineException -> Restart: [${ce.getMessage}].")
        Restart
      case fe: FrothingException =>
        println(s"FrothingException -> Stop: [${fe.getMessage}]")
        Stop
      case e: Exception =>
        println(s"Exception Unknown -> Escalate: [${e.getMessage}]")
        Escalate
    }

    def collectGrass(grass: Grass): Unit = grassActor ! GrassMsg(grass)

    def heatWater(): Unit = heatWaterActor ! GetWaterAndHeatMsg(water)

    def combine(): Unit = combineActor ! CombineTeaMsg(groundGrass, water)

    def receive: Receive = {
      case TeaInit(grass, time) =>
        val timeStarted: String  = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        println(s"Starting TeaInit ${timeStarted}")
        start = time
        collectGrass(grass)
        heatWater()
        println("TeaInit started")
      case GrassDoneMsg(ground) =>
        println(s"GrassDoneMsg [${ground}]")
        groundGrass = Some(ground)
        if (HeatWaterActor.ifTemperatureOkay(water)) {
          println(s"Temperature is OK so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineTeaMsg(groundGrass, water)
        }
      case HeatWaterDoneMsg(water) =>
        println(s"HeatWaterDoneMsg [${water.temperature}]")
        if (GrassActor.groundGrass(groundGrass)) {
          println(s"Ground grass are OK so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineTeaMsg(groundGrass, water)
        }
      case Tea(tea) =>
        coffeeMachine ! Tea(s"Here is your [$tea]. Time duration for processing: ${System.currentTimeMillis() - start} milliseconds")
    }
  }

}
