package com.scanner.service.core.actor

import akka.actor.{Actor, ActorSelection}
import com.typesafe.config.Config

/**
  * Created by igorbylov on 28.06.17.
  */
trait ActorSelecting { this: Actor =>

  def locateActor(host: String, port: String, name: String): ActorSelection =
    context.actorSelection(s"akka.tcp://scanner@$host:$port/user/$name")

  def locateActor(actorAddressConfig: Config): ActorSelection = {
    val host = actorAddressConfig.getString("host")
    val port = actorAddressConfig.getString("port")
    val name = actorAddressConfig.getString("name")
    context.actorSelection(s"akka.tcp://scanner@$host:$port/user/$name")
  }

}
