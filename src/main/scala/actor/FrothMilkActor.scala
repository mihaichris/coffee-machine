package actor

import akka.actor.{Actor, ActorRef}

object FrothMilkActor {

  type Milk = String
  type FrothedMilk = String

  case class FrothMilkMsg(milk: Milk)

  case class FrothMilkDoneMsg(milk: Milk)

  case class FrothingException(msg: String) extends Exception(msg)

  class FrothMilkActor(actorRef: ActorRef) extends Actor {
    def receive: Receive = {
      case FrothMilkMsg(milk) => {
        println("Milk frothing system engaged!")
        Thread.sleep(3000)
        println(s"Shutting down milk frothing system. with actor [${actorRef.path}]")
        actorRef ! FrothMilkDoneMsg(new FrothedMilk(s"frothed $milk"))
      }
    }
  }

  def frothedMilk(frothedMilk: Option[Any]): Boolean = {
    println(s"Checking frothed Milk of ${frothedMilk}")
    frothedMilk match {
      case Some(_) => true
      case None => false
    }
  }

  def espresso(espresso: Option[Any]): Boolean = {
    println(s"Checking espresso of ${espresso}")
    espresso match {
      case Some(_) => true
      case None => false
    }
  }
}
