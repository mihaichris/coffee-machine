package actor

import akka.actor.{Actor, ActorRef}

object GrindActor {
  type CoffeeBeans = String
  type GroundCoffee = String

  case class GrindMsg(beans: CoffeeBeans)

  case class GrindDoneMsg(ground: GroundCoffee)

  case class GrindingException(msg: String) extends Exception(msg)

  class GrindActor(actorRef: ActorRef) extends Actor {
    def receive: Receive = {
      case GrindMsg(beans) =>
        println("Start grinding...")
        Thread.sleep(3000)
        if (beans == "Baked beans") throw GrindingException("We could not grind this kind of beans.")
        println(s"Finished grinding...  with actor for ${actorRef.path}")
        actorRef ! GrindDoneMsg(new GroundCoffee(s"Ground coffee of $beans"))
    }
  }

  def groundBeans(groundCoffee: Option[Any]): Boolean = {
    println(s"Checking ground beans of ${groundCoffee}")
    groundCoffee match {
      case Some(value) => true
      case None => false
    }
  }
}
