package actor

import akka.actor.{Actor, ActorRef}

object GrassActor {
  type Grass = String
  type GroundGrass = String

  case class GrassMsg(grass: Grass)

  case class GrassDoneMsg(grass: GroundGrass)

  case class GrassException(msg: String) extends Exception(msg)

  class GrassActor(actorRef: ActorRef) extends Actor {
    def receive: Receive = {
      case GrassMsg(grass) => {
        println("Start collecting herbs...")
        Thread.sleep(2000)
        println(s"Finished collecting herbs...  with actor [${actorRef.path}]")
        actorRef ! GrassDoneMsg(new GroundGrass(s"herbs of $grass"))
      }
    }
  }

  def groundGrass(groundGrass: Option[Any]): Boolean = {
    println(s"Checking herbs of ${groundGrass}")
    groundGrass match {
      case Some(value) => true
      case None => false
    }
  }
}
