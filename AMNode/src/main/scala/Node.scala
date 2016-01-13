import java.util.Calendar
import akka.actor._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable.{ Seq => MutableSeq, _}
import scala.language.postfixOps
import scala.util.Random

case object GetStatistic
case object NodeStarted
case object SendMessage
case object Message
case object NodeStop
case class  Statistic(n:Long)
case class  NodeStart(t:FiniteDuration)

object Node extends App{
  val system = ActorSystem("ActorSystem")
  val node = system.actorOf(Props[Node], name = "node")
  node ! NodeStarted
}

class Node extends Actor with ActorLogging{
  import Node._
  val server = system.actorSelection("akka.tcp://ActorSystem@127.0.0.1:2552/user/server")
  var actors: Seq[ActorRef] = Seq.empty
  var messageScheduler: Cancellable = system.scheduler.schedule(0 milliseconds, 0 microseconds, node, SendMessage)
  var timeOfReceipt:Buffer[Long] = Buffer()

  def receive = {
    case SendMessage if actors.length != 0 => actors(Random.nextInt(actors.length)) ! Message
    case GetStatistic => server ! Statistic(timeOfReceipt.takeWhile(Calendar.getInstance().getTimeInMillis - _ < 1000).length)
    case Message =>
      timeOfReceipt.+=:(Calendar.getInstance().getTimeInMillis)
      val temp = timeOfReceipt.takeWhile(timeOfReceipt(0) - _ < 1000)
      timeOfReceipt = temp
      println(s"Received ${temp.length} messages at last second")
    case newActors: MutableSeq[ActorRef] => actors = newActors.filter(_ != node)
    case NodeStart(t) =>
      messageScheduler.cancel()
      messageScheduler = system.scheduler.schedule(0 milliseconds, t, node, SendMessage)
    case NodeStop => messageScheduler.cancel()
    case NodeStarted => server ! NodeStarted
    case _ =>
  }
}