package com.mbcu.hitbtc.mmm.models.internal

import com.mbcu.hitbtc.mmm.models.response.Order
import play.api.libs.json._
import play.api.libs.functional.syntax._


case class Credentials (pKey : String, nonce: String, signature : String)
object Credentials {
  implicit val jsonFormat = Json.format[Credentials]

}

case class Env(email : String, logSeconds : Int)
object Env {
  implicit val jsonFormat = Json.format[Env]
}

case class Bot (
pair              : String,
startMiddlePrice  : BigDecimal,
gridSpace         : BigDecimal,
buyGridLevels     : Int,
sellGridLevels    : Int,
buyOrderQuantity  : BigDecimal,
sellOrderQuantity : BigDecimal,
qtyScale          : Int,
strategy          : String

)
object Bot {
  implicit val jsonFormat = Json.format[Bot]

  object Implicits {
    implicit val botWrites = new Writes[Bot] {
      def writes(bot: Bot): JsValue = Json.obj(
        "pair" -> bot.pair,
        "startMiddlePrice" -> bot.startMiddlePrice,
        "gridSpace" -> bot.gridSpace,
        "buyGridLevels" -> bot.buyGridLevels,
        "sellGridLevels" -> bot.sellGridLevels,
        "buyOrderQuantity" -> bot.buyOrderQuantity,
        "sellOrderQuantity" -> bot.sellOrderQuantity,
        "qtyScale" -> bot.qtyScale,
        "strategy" -> bot.strategy
      )
    }

    implicit val botReads: Reads[Bot] = (
      (JsPath \ "pair").read[String] and
      (JsPath \ "startMiddlePrice").read[BigDecimal] and
      (JsPath \ "gridSpace").read[BigDecimal] and
      (JsPath \ "buyGridLevels").read[Int] and
      (JsPath \ "sellGridLevels").read[Int] and
      (JsPath \ "buyOrderQuantity").read[BigDecimal] and
      (JsPath \ "sellOrderQuantity").read[BigDecimal] and
      (JsPath \ "qtyScale").read[Int] and
      (JsPath \ "strategy").read[String]
      ) (Bot.apply _)
  }
}

case class Config (credentials: Credentials, env : Env, bots : List[Bot])
object Config {
  implicit val jsonFormat = Json.format[Config]

}