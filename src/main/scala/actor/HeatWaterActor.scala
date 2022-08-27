package actor

import actor.WaterStorageActor._
import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorRef, OneForOneStrategy}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

object HeatWaterActor {

  case class WaterBoilingException(msg: String) extends Exception(msg)

  class HeatWaterActor(actorRef: ActorRef) extends Actor {

    implicit val timeout: Timeout = Timeout(10.seconds)
    private val waterStorageActor = context.actorOf(WaterStorageActor.props, "WaterStorageActor")

    override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
      case we: WaterLackException =>
        println(s"WaterLackException -> Escalate: [${we.getMessage}]")
        Escalate
      case e: Exception =>
        println(s"Exception Unknown -> Restart: [${e.getMessage}]")
        Restart
    }

    def receive: Receive = {
      case GetWaterAndHeatMsg(water) =>
        waterStorageActor ! GetWaterMsg(water)
      case WaterMsg(water) =>
        Thread.sleep(3000)
        val waterHeated = Water(water.qtd, 85)
        println(s"The water is hot! sending HeatWaterDoneMsg with temperature ${waterHeated.temperature} degrees")
        actorRef ! HeatWaterDoneMsg(waterHeated)
    }

    override def preStart() {
      initWaterStorage()
    }

    override def postRestart(reason: Throwable) {
      println("HeatWaterActor postRestart")
    }

    def initWaterStorage() {
      waterStorageActor ! InitWaterStorageMsg
    }
  }

  def ifTemperatureOkay(water: Water): Boolean = {
    println(s"Checking temperature.... [${water.temperature}]")
    (80 to 85).contains(water.temperature)
  }
}
