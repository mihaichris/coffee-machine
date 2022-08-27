package actor

import actor.BrewActor.Espresso
import actor.CappuccinoActor.Cappuccino
import actor.FrothMilkActor.FrothedMilk
import actor.GrassActor.GroundGrass
import actor.TeaActor.Tea
import actor.WaterStorageActor.Water
import akka.actor.{Actor, ActorRef, Props}

import scala.util.Random

object CombineActor {

  val props: Props = Props[CombineActor]

  case class CombineException(msg: String) extends Exception(msg)

  case class CombineCappuccinoMsg(espresso: Option[Espresso], frothedMilk: Option[FrothedMilk])

  case class CombineTeaMsg(groundGrass: Option[GroundGrass], water: Water)

  class CombineActor(actorRef: ActorRef) extends Actor {
    def receive: Receive = {
      case CombineCappuccinoMsg(espresso, frothedMilk) =>
        if (failCombining()) throw CombineException(s"The Coffee Machine could not combine [$espresso] with frothed milk [$frothedMilk]. =(")
        println(s"Combine espresso [$espresso] with frothed milk [$frothedMilk], with [${actorRef.path}]")
        Thread.sleep(2000)
        actorRef ! Cappuccino(s"Cappuccino [$espresso] with [$frothedMilk].")

      case CombineTeaMsg(groundGrass, hotWater) =>
        if (failCombining()) throw CombineException(s"The Coffee Machine could not combine [$groundGrass] with water [$hotWater]. =(")
        println(s"Combine tea [$groundGrass] with water [$hotWater], with [${actorRef.path}]")
        Thread.sleep(2000)
        actorRef ! Tea(s"Tea [$groundGrass] with water [$hotWater]")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      val combiningMessage = message.getOrElse("")
      println(s"Restart combining the: [$combiningMessage]")
      self ! combiningMessage
      super.preRestart(reason, message)
    }

    private def failCombining(): Boolean = Random.nextInt(2000) % 2 != 0
  }

}
