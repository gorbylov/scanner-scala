package com.scanner.service.core.utils

import java.lang.Math._

/**
  * Created by Iurii on 10-07-2017.
  */
object MathUtils {

  private implicit def bigDecimalToDouble(bd: BigDecimal): Double = bd.doubleValue()

  /**
    *  Gives great-circle distances between two points on a sphere from their longitudes and latitudes
    */
  def haversineDistance(pointA: (BigDecimal, BigDecimal), pointB: (BigDecimal, BigDecimal)): BigDecimal = {
    val radius = 6372.8 //radius in km
    val deltaLat = toRadians(pointB._1 - pointA._1)
    val deltaLng = toRadians(pointB._2 - pointA._2)

    val a = pow(sin(deltaLat / 2), 2) + pow(sin(deltaLng / 2), 2) * cos(toRadians(pointA._1)) * cos(toRadians(pointB._1))
    val circleDistance = 2 * asin(sqrt(a))

    radius * circleDistance
  }

}
