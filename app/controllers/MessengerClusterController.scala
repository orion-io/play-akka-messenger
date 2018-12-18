package controllers

import java.io.File

import akka.actor.{ActorSystem, Address}
import akka.cluster.Cluster
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import com.typesafe.config.ConfigFactory
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws
import play.api.libs.ws.WSClient
import play.api.mvc.{InjectedController, Result, Results}

import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class MessengerClusterController @Inject()(private val configuration: Configuration,
                                           private val applicationSystem: ActorSystem,
                                           private val applicationLifecycle: ApplicationLifecycle,
                                           private val wsClient: WSClient) extends InjectedController {

  import concurrent.ExecutionContext.Implicits.global

  private val logger: Logger = Logger(classOf[MessengerClusterController])
  private val runLocal = configuration.getOptional[Int]("run-local").contains(1)
  private val messegnerSystem = if(runLocal) initLocal() else initAws()

  def status = Action { _ => Results.Ok}

  private def initLocal(): Future[ActorSystem] = Future {
    val configString =
      s"""
         |akka {
         |  remote.artery.canonical.hostname = 127.0.0.1
         |  remote.artery.canonical.port = ${configuration.get[Int]("cluster-port")}
         |  remote.artery.bind.port = ${configuration.get[Int]("cluster-port")}
         |}""".stripMargin


    val config = ConfigFactory.parseFile(new File("conf/akka-messenger.conf")).resolve().withFallback(ConfigFactory.parseString(configString))
    val system = ActorSystem(akka.messenger.api.systemName, config)
    val cluster = Cluster(system)
    println(cluster.selfAddress)
    cluster.join(Address(protocol = "akka", system = akka.messenger.api.systemName, host = "127.0.0.1", port = configuration.get[Int]("join-local-cluster-port")))

    applicationLifecycle.addStopHook { () =>
      Future {
        cluster.down(cluster.selfAddress)
      }
    }

    system.actorOf(akka.messenger.actors.MessengerActor.props, akka.messenger.api.systemName)

    system
  }.recover {
    case e: Exception =>
      println(e.toString)
      throw e
  }

  private def initAws(): Future[ActorSystem] = {
    wsClient
      .url("http://169.254.169.254/latest/meta-data/local-ipv4")
      .withRequestTimeout(10.seconds)
      .get().map { response =>
      val ip = response.body

      val configString =
        s"""
           |akka {
           | remote.artery.canonical.hostname = $ip
           | remote.artery.canonical.port = ${configuration.get[Int]("cluster-port")}
           | remote.artery.bind.port = ${configuration.get[Int]("cluster-port")}
           | management.hostname = $ip
           |}""".stripMargin

      val config = ConfigFactory.parseFile(new File("conf/akka-messenger.conf")).resolve().withFallback(ConfigFactory.parseString(configString))
      val system = ActorSystem(akka.messenger.api.systemName, config)

      AkkaManagement(system).start()
      ClusterBootstrap(system).start()
      val cluster = Cluster(system)

      applicationLifecycle.addStopHook { () =>
        Future {
          cluster.down(cluster.selfAddress)
        }
      }

      system.actorOf(akka.messenger.actors.MessengerActor.props, akka.messenger.api.systemName)

      system
    }.recover {
      case e: Exception =>
        logger.error("could not initialize akka messenger cluster node", e)
        throw e
    }
  }
}
