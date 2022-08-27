package actor

import actor.GrindActor.GroundCoffee
import actor.WaterStorageActor.Water
import akka.actor.{Actor, ActorRef}

object BrewActor {

  type Espresso = String

  case class BrewMsg(coffee: Option[GroundCoffee], heatedWater: Water)

  case class EspressoMsg(espresso: Espresso)

  case class BrewingException(msg: String) extends Exception(msg)

  class BrewActor(actorRef: ActorRef) extends Actor {
    def receive: Receive = {
      case BrewMsg(coffee, heatedWater) =>
        println(s"Happy brewing :) of water quantity ${heatedWater.qtd} with ${heatedWater.temperature} degrees and ${coffee}")
        Thread.sleep(2000)
        println(s"It's brewed! with ${actorRef.path}")
        actorRef ! EspressoMsg(new Espresso("espresso"))
    }
  }

}
