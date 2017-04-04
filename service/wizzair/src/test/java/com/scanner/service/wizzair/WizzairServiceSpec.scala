package com.scanner.service.wizzair

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.scanner.query.api.GetOneWayFlightsQuery
import org.scalatest.{Matchers, WordSpecLike}

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairServiceSpec extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender
  with WordSpecLike
  with Matchers {

  "WizzairService actor" should {

    val wizzairService = TestActorRef(new WizzairService())

    wizzairService ! GetOneWayFlightsQuery("IEV", "BUD", LocalDate.now().plusDays(1), LocalDate.now().plusDays(1).plusMonths(1), Seq(), "UAH")

  }

  }
