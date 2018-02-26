package com.mbcu.hitbtc.mmm.sequences

import java.math.MathContext
import java.util.Collections
import java.util.stream.IntStream

import com.mbcu.hitbtc.mmm.models.internal.Bot
import com.mbcu.hitbtc.mmm.models.request.{NewOrder, NewOrderParam}
import com.mbcu.hitbtc.mmm.models.response.Order
import com.mbcu.hitbtc.mmm.sequences.Strategy.Movement.Movement
import com.mbcu.hitbtc.mmm.utils.MyUtils
import play.api.libs.json.Format

import scala.util.Random

object Strategy {
  val mc : MathContext = MathContext.DECIMAL64
  val ZERO = BigDecimal("0")
  val ONE = BigDecimal("1")
  val CENT = BigDecimal("100")

  object Movement extends Enumeration {
    type Movement = Value
    val UP, DOWN  = Value
  }


  def seed(qty0 : BigDecimal, unitPrice0 : BigDecimal, symbol : String, levels : Integer, gridSpace : BigDecimal, side : String, isPulledFromOtherSide : Boolean, strategy : String) : Seq[NewOrder] = {
    strategy match {
      case "ppt" => {
        pptSeed(qty0, unitPrice0, symbol, levels, gridSpace, side, isPulledFromOtherSide)
      }
      case _ => Seq.empty
    }
  }

  def counter(qty0 : BigDecimal, unitPrice0 : BigDecimal, symbol : String, gridSpace : BigDecimal, side : String, strategy : String) : Seq[NewOrder] = {
    val newSide = if (side == "buy") "sell" else "buy"
    strategy match {
      case "ppt" => pptSeed(qty0, unitPrice0, symbol, 1, gridSpace, newSide, isPulledFromOtherSide = false)
      case _ => Seq.empty
    }
  }

   def pptSeed (qty0 : BigDecimal, unitPrice0 : BigDecimal, symbol : String, levels : Int, gridSpace : BigDecimal, side : String, isPulledFromOtherSide : Boolean) : Seq[NewOrder] = {
     val mtp = ONE + gridSpace(mc) / CENT
     val range = if(isPulledFromOtherSide) 3 else 2
     (range until (levels + range))
       .map(n => {
         val rate = Collections.nCopies(n, ONE).stream().reduce((x, y) => x * mtp).get()
         val movement = if (side == "buy") Movement.DOWN else Movement.UP
         val (unitPrice1, qty1) = ppt(unitPrice0, qty0, rate, movement)
         val newOrderParam = NewOrderParam(MyUtils.clientOrderId(symbol, side), symbol, side, unitPrice1, qty1)
         newOrderParam
       })
       .filter(_.price > ZERO)
       .filter(_.quantity > ZERO)
       .map(p => NewOrder(p.clientOrderId, p))
   }


//  def pptSeed (start : Order, levels : Integer, gridSpace : BigDecimal, side : String, isPulledFromOtherSide : Boolean) : Seq[NewOrder] = pptSeed(start.quantity, start.price, start.symbol, levels, gridSpace, side, isPulledFromOtherSide)


  def ppt(unitPrice0 : BigDecimal, qty0 : BigDecimal, rate : BigDecimal, movement: Movement ): (BigDecimal, BigDecimal) ={
    val unitPrice1 = if (movement == Movement.DOWN) unitPrice0(mc) / rate else unitPrice0 * rate
    val sqrt = MyUtils.sqrt(rate)
    val qty1 = if (movement == Movement.DOWN) qty0 * sqrt else qty0(mc) / sqrt
    (unitPrice1, qty1)
  }
  /*

  		MathContext mc = MathContext.DECIMAL64;
		BigDecimal mtp = bot.getGridSpace();
		BigDecimal unitPrice0	= isBuySeed ? last.buy.unitPrice  : last.sel.unitPrice;
		BigDecimal qty0 = isBuySeed ? last.buy.qty : last.sel.qty;

		int range = isBuySeed ? last.isBuyPulledFromSel ? 3 : 2 : last.isSelPulledFromBuy ? 3 : 2;
		List<RLOrder> res = IntStream
				.range(range, levels + range)
				.mapToObj(n -> {
					BigDecimal rate = Collections.nCopies(n, BigDecimal.ONE).stream().reduce((x, y) -> x.multiply(mtp, mc)).get();
					BigDecimal unitPrice1 = isBuySeed ? unitPrice0.divide(rate, mc) : unitPrice0.multiply(rate, mc);
					BigDecimal sqrt  = MyUtils.bigSqrt(rate);
					BigDecimal qty1 = isBuySeed ? qty0.multiply(sqrt, mc): qty0.divide(sqrt, mc);
					if (unitPrice1.compareTo(BigDecimal.ZERO) <= 0) {
						log.severe("RLOrder.buildBuySeedPct rate below zero. Check config for the pair " + bot.getPair());
					}
					BigDecimal total1 = qty1.multiply(unitPrice1, mc);
					Amount qtyAmount1		= bot.base.add(qty1);
					Amount totalAmount1  = RLOrder.amount(total1, Currency.fromString(bot.quote.currencyString()), AccountID.fromAddress(bot.quote.issuerString()));
					Direction direction1 = isBuySeed ? Direction.BUY: Direction.SELL;
					RLOrder buy = RLOrder.rateUnneeded(direction1, qtyAmount1, totalAmount1);
					return buy;
			})
	   .filter(o -> o.getQuantity().value().compareTo(BigDecimal.ZERO) > 0)
.filter(o -> o.getTotalPrice().value().compareTo(BigDecimal.ZERO) > 0)

   (XRPBTC,buys : 3
quantity:1 price:0.00009000
quantity:1 price:0.00008000
quantity:1 price:0.00007000
sells : 3
quantity:1 price:0.10000000
quantity:1 price:0.10000000
quantity:1 price:2.00000000
)
   */
}
