import Property._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import scala.collection.mutable._
import scala.concurrent.duration._
import scala.util.Random

case object NodeStarted
case object NodeStop
case object KillNodes
case object GetStatistic
case object StartNodes
case class  Statistic(n:Long)
case class  NodeStart(t:FiniteDuration)

object Server extends App {
  val system = ActorSystem("ActorSystem")
  val actors = MutableList.empty[ActorRef]
  val server = system.actorOf(Props[Server], name = "server")
  var statistic = MutableList.empty[Long]
  var s = ""

  def powerTest(nc:Int,i:Int,maxPower:Long):Cancellable = {
    statistic = MutableList.empty[Long]
    setNodeCount(nc)
    setInterval(i)
    system.scheduler.scheduleOnce(3 seconds)(actors.foreach(_ ! GetStatistic))
    system.scheduler.scheduleOnce(5 seconds){
      val statSum = statistic.sum
      statistic.foreach((x:Long) => print(x.toString+" "))
      println()
      if(statSum < maxPower){
        println((getNodeCount,getInterval,maxPower))
        setNodeCount(0)
      } else {
        if(Random.nextBoolean()&&nc<actors.length) powerTest(nc+1,i,statSum)
        else powerTest(nc,if(i>10) i-10 else 0,statSum)
      }
    }
  }

  while(s != "kill") {
    s = scala.io.StdIn.readLine
    s split " " match {
      case Array("interval",t)  => Property.setInterval(t toInt)
      case Array("powertest") => powerTest(2,100,0)
      case Array("count",c) => setNodeCount(c toInt)
      case Array("kill") => server ! KillNodes
      case x => println(x mkString)
    }
  }
}

class Server extends Actor with ActorLogging {
  import Server._

  def receive = {
    case NodeStarted => actors += sender
    case Statistic(s) => statistic += s
    case StartNodes =>
      val runningActors = actors.take(getNodeCount)
      actors.drop(getNodeCount).foreach(_ ! NodeStop)
      runningActors.foreach(_ ! runningActors)
      runningActors.foreach(_ ! NodeStart(getInterval milliseconds))
    case KillNodes => (actors :+ server) foreach (_ ! Kill)
    case _ =>
   }
}