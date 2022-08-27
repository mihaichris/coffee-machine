package actor

import `enum`.{CoffeeBeansFlavours, CoffeeMachineOptions, TeaHerbs}
import actor.CappuccinoActor.CappuccinoMsg
import actor.GrassActor.Grass
import actor.GrindActor.CoffeeBeans
import actor.TeaActor.TeaMsg
import akka.actor.{Actor, ActorRef, OneForOneStrategy}
import akka.pattern.ask
import akka.util.Timeout
import infrastructure.console.{Console, SystemConsole}

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object UserInteractionActor {

  type CoffeeMachineOption = String

  case class AskAgainForBeverage()

  case class InitUserInteraction()

  case class ChooseOption(option: CoffeeMachineOption)

  val console: Console = new SystemConsole();

  implicit val timeout: Timeout = Timeout(3 seconds)

  class UserInteractionActor(actorRef: ActorRef) extends Actor {
    override def receive: Receive = {
      case InitUserInteraction() => askUserForBeverage()
      case AskAgainForBeverage() => askUserAgainForBeverage()
    }

    private def askUserAgainForBeverage(): Unit = {
      print("Do you want another beverage? [yes/no]:")
      val userInputForAnotherBeverage = console.read().trim();
      if (userInputForAnotherBeverage.equals("yes")) {
        print("What beverage do you want, coffee or tea:")
        val userInputForCoffeeOrTea = console.read();
        if (userInputForCoffeeOrTea.equals(CoffeeMachineOptions.coffee.toString)) {
          for (coffeeFlavour <- CoffeeBeansFlavours.values) {
            println(coffeeFlavour.toString + " : " + coffeeFlavour.id)
          }
          print("Choose a code number:")
          val userInput = console.read().trim();
          val coffeeBeans = CoffeeBeansFlavours.apply(userInput.toInt)
          if (coffeeBeans == null) {
            print("There is no such flavour. Aborting!")
            System.exit(0);
          }
          this.askForCappuccino(coffeeBeans.toString)
        } else if (userInputForCoffeeOrTea.equals(CoffeeMachineOptions.tea.toString)) {
          println("What tea flavour do you want:")
          for (teaFlavour <- TeaHerbs.values) {
            println(teaFlavour.toString + " : " + teaFlavour.id)
          }
          print("Choose a code number:")
          val userInput = console.read().trim();
          val teaHerbs = TeaHerbs.apply(userInput.toInt)
          if (teaHerbs == null) {
            print("There is no such flavour. Aborting!")
            System.exit(0);
          }
          this.askForTea(teaHerbs.toString)
        } else {
          println("This coffee machine can not make: " + userInputForCoffeeOrTea + "....Aborting !");
          System.exit(0);
        }
      } else if (userInputForAnotherBeverage.equals("no")) {
        print("Goodbye!")
        System.exit(0);
      }
    }

    private def askUserForBeverage(): Unit = {
      print("Welcome to my coffee machine, choose if you want coffee or tea:")
      val userInputForCoffeeOrTea = console.read().trim();
      if (userInputForCoffeeOrTea.equals(CoffeeMachineOptions.coffee.toString)) {
        for (coffeeFlavour <- CoffeeBeansFlavours.values) {
          println(coffeeFlavour.toString + " : " + coffeeFlavour.id)
        }
        print("Choose a code number:")
        val userInput = console.read().trim();
        val coffeeBeans = CoffeeBeansFlavours.apply(userInput.toInt)
        this.askForCappuccino(coffeeBeans.toString)
      } else if (userInputForCoffeeOrTea.equals(CoffeeMachineOptions.tea.toString)) {
        println("What tea flavour do you want:")
        for (teaFlavour <- TeaHerbs.values) {
          println(teaFlavour.toString + " : " + teaFlavour.id)
        }
        print("Choose a code number:")
        val userInput = console.read().trim();
        val teaHerbs = TeaHerbs.apply(userInput.toInt)
        this.askForTea(teaHerbs.toString)
      } else {
        println("This coffee machine can not make: " + userInputForCoffeeOrTea);
        System.exit(0);
      }
    }

    private def askForCappuccino(coffeeBeansFlavour: String): Unit = {
      val futureCappuccino: Future[Any] = actorRef ? CappuccinoMsg(new CoffeeBeans(coffeeBeansFlavour),  System.currentTimeMillis())
      val resultCappuccino = Await.result(futureCappuccino, timeout.duration).asInstanceOf[String]
      println(s"Coffee machine says : $resultCappuccino")
    }

    private def askForTea(teaHerbs: String): Unit = {
      val futureTea: Future[Any] = actorRef ? TeaMsg(new Grass(teaHerbs), System.currentTimeMillis())
      val resultTea = Await.result(futureTea, timeout.duration).asInstanceOf[String]
      println(s"Coffee machine says: $resultTea")
    }

    override def preStart(): Unit = self ! InitUserInteraction()
  }
}
