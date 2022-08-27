package actor

import akka.actor.{Actor, ActorSystem, Props}

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.DurationInt

object WaterStorageActor {

  val props: Props = Props[WaterStorageActor]

  case class Water(qtd: Int, temperature: Int)

  case class GetWaterMsg(water: Water)

  case class WaterMsg(water: Water)

  case class FillWaterMsg()

  case class InitWaterStorageMsg()

  case class HeatWaterMsg(water: Water)

  case class GetWaterAndHeatMsg(water: Water)

  case class HeatWaterDoneMsg(water: Water)

  case class WaterLackException(msg: String) extends Exception(msg)

  private var waterStorageQtd: AtomicInteger = new AtomicInteger()

  val system: ActorSystem = ActorSystem("WaterStorageActor")
  private val waterStorageActor = system.actorOf(Props[WaterStorageActor], "WaterStorageActor")
  val capacity: Long = 50

  class WaterStorageActor() extends Actor {
    def receive: Receive = {
      case InitWaterStorageMsg => WaterStorageActor.initWaterStorage()
      case GetWaterMsg(_) =>
        this.synchronized {
          if (waterStorageQtd.get < 4) {
            throw WaterLackException(s"There is not enough water in the WaterStorage: [${waterStorageQtd.get}]")
          } else {
            for (x <- 1 to 4) {
              println(s"Decrementing storage $x")
              waterStorageQtd.decrementAndGet()
            }
            Thread.sleep(100)
            sender ! WaterMsg(Water(4, 20))
          }
        }
      case FillWaterMsg =>
        //        println(s"Filling WaterStorage [${waterStorageQtd.get}] ....")
        if (waterStorageQtd.get < capacity) waterStorageQtd.addAndGet(1)
    }
  }

  def initWaterStorage() {
    waterStorageQtd = new AtomicInteger(0)
    import system.dispatcher
//    println(s"waterStorageActor [${waterStorageActor.path}]")
    system.scheduler.scheduleWithFixedDelay(0 milliseconds, 3000 milliseconds, waterStorageActor, FillWaterMsg)
//     cancellable.cancel()
  }
}
