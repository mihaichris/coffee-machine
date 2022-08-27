package actor

import actor.BrewActor.{BrewActor, BrewMsg, Espresso, EspressoMsg}
import actor.CombineActor.{CombineActor, CombineCappuccinoMsg, CombineException}
import actor.FrothMilkActor.{FrothMilkActor, FrothMilkDoneMsg, FrothMilkMsg, FrothedMilk, FrothingException, Milk}
import actor.GrindActor.{CoffeeBeans, GrindActor, GrindDoneMsg, GrindMsg, GrindingException, GroundCoffee}
import actor.HeatWaterActor.{HeatWaterActor, WaterBoilingException}
import actor.WaterStorageActor.{GetWaterAndHeatMsg, HeatWaterDoneMsg, Water, WaterLackException}
import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props}

import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

object CappuccinoActor {

  case class CappuccinoInit(beans: CoffeeBeans, time: Long)

  case class CappuccinoMsg(beans: CoffeeBeans, time: Long)

  case class Cappuccino(value: String)

  class CappuccinoActor(coffeeMachine: ActorRef) extends Actor {
    private val grindActor = context.actorOf(Props(new GrindActor(self)), "GrindActor")
    private val heatWaterActor = context.actorOf(Props(new HeatWaterActor(self)), "HeatWaterActor")
    private val frothMilkActor = context.actorOf(Props(new FrothMilkActor(self)), "FrothMilkActor")
    private val brewActor = context.actorOf(Props(new BrewActor(self)), "BrewActor")
    private val combineActor = context.actorOf(Props(new CombineActor(self)), "CombineActor")
    var water: Water = Water(0, 20)
    var groundCoffee: Option[GroundCoffee] = None
    var frothedMilk: Option[FrothedMilk] = None
    var espresso: Option[Espresso] = None
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

    def grind(beans: CoffeeBeans): Unit = grindActor ! GrindMsg(beans)

    def heatWater(): Unit = heatWaterActor ! GetWaterAndHeatMsg(water)

    def frothMilk(): Unit = frothMilkActor ! FrothMilkMsg(new Milk("milk"))

    def brew(): Unit = brewActor ! BrewMsg(groundCoffee, water)

    def combine(): Unit = combineActor ! CombineCappuccinoMsg(espresso, frothedMilk)

    def receive: Receive = {
      case CappuccinoInit(beans, time) =>
        val timeStarted: String  = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())
          .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        println(s"Starting CappuccinoInit $timeStarted.")
        start = time
        grind(beans)
        heatWater()
        frothMilk()
        println("CappuccinoInit started..")
      case GrindDoneMsg(ground) =>
        println(s"GrindDoneMsg [${ground}]")
        groundCoffee = Some(ground)
        if (HeatWaterActor.ifTemperatureOkay(water)) {
          println(s"Temperature is OK so we can brew =) with actor [${brewActor.path}]")
          brew()
        }
      case HeatWaterDoneMsg(w) =>
        println(s"HeatWaterDoneMsg [${w.qtd}] of water [${w.temperature}] degrees")
        water = w
        if (GrindActor.groundBeans(groundCoffee)) {
          println(s"Ground beans are OK so we can brew =) with [${brewActor.path}]")
          brew()
        }
      case EspressoMsg(e) =>
        println(s"EspressoMsg [${e}]")
        espresso = Some(e)
        if (FrothMilkActor.frothedMilk(frothedMilk)) {
          println(s"Milk is frothed so we can combine =) with [${combineActor.path}]")
          combine()
        }
      case FrothMilkDoneMsg(milk) =>
        println(s"FrothMilkDoneMsg [${milk}]")
        frothedMilk = Some(milk)
        if (FrothMilkActor.espresso(espresso)) {
          println(s"Espresso is OK so we can combine =) with [${combineActor.path}]")
          combine()
        }
      case Cappuccino(cappuccino) =>
        coffeeMachine ! Cappuccino(s"Here is your [$cappuccino] in ${System.currentTimeMillis() - start} milliseconds")
    }
  }

}
