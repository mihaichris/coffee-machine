import actor.CappuccinoActor.{Cappuccino, CappuccinoActor, CappuccinoInit, CappuccinoMsg}
import actor.TeaActor.{Tea, TeaActor, TeaInit, TeaMsg}
import actor.UserInteractionActor.{AskAgainForBeverage, UserInteractionActor}
import akka.actor.{Actor, ActorSystem, Props}
import exception.ExceptionProcessingOrder

object CoffeeMachine extends App {

  val system = ActorSystem("CoffeeMachineSupervisor")
  val coffeeMachine = system.actorOf(Props(new CoffeeMachine()), "CoffeeMachineSupervisor")
  class CoffeeMachine extends Actor {
    private val userInteractionActor = context.actorOf(Props(new UserInteractionActor(self)), "UserInteractionActor")
    private val cappuccinoActor = context.actorOf(Props(new CappuccinoActor(self)), "CappuccinoActor")
    private val teaActor = context.actorOf(Props(new TeaActor(self)), "TeaActor")

    def receive: Receive = {
      case ExceptionProcessingOrder(reason) =>
        userInteractionActor ! AskAgainForBeverage()
      case CappuccinoMsg(beans, time) =>
        cappuccinoActor ! CappuccinoInit(beans, time)
        sender ! "We are making your Cappuccino..."
      case Cappuccino(value) =>
        println(value)
        println();
        userInteractionActor ! AskAgainForBeverage()
      case TeaMsg(grass, time) =>
        teaActor ! TeaInit(grass, time)
        sender ! "We are making your Tea..."
      case Tea(value) =>
        println(value)
        println();
        userInteractionActor ! AskAgainForBeverage()
    }

    override def postStop() {
      println("Stopping Cappuccino Actor....")
      this.context.stop(cappuccinoActor)

      println("Stopping Tea Actor....")
      this.context.stop(teaActor)
    }
  }
}
