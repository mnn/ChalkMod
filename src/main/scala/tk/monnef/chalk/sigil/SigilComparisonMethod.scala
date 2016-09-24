package tk.monnef.chalk.sigil


import tk.monnef.chalk.core.common._
import scalaz._
import Scalaz._
import scala.reflect.ClassTag
import shapeless._
import poly._
import shapeless.syntax.std.tuple._
import shapeless.syntax.std.traversable._

import Sigils._
import net.minecraft.util.EnumFacing
import tk.monnef.chalk.sigil.ComparisonImprovement.ComparisonImprovement

object ComparisonImprovement extends Enumeration {
  type ComparisonImprovement = Value
  val Min, Max = Value
}

trait SigilComparisonMethod {
  def calculate(first: Canvas, second: Canvas): Float

  def calculateScore(first: Canvas, second: Canvas): Float

  def improvement: ComparisonImprovement
}

object MeanSquareDifference extends SigilComparisonMethod {
  def calculate(first: Canvas, second: Canvas): Float = {
    val size = first.length * first.head.length
    // possibly very inefficient, rewrite uglier?
    val differences = first.flatten.toList.zip(second.flatten.toList)
                      .map { case (f: Sigil, s: Sigil) => Math.pow(f.toFloat - s, 2) }
    val sum = differences.sum
    sum.toFloat / size
  }

  override def improvement: ComparisonImprovement = ComparisonImprovement.Min

  override def calculateScore(first: Canvas, second: Canvas): Float = {
    val msd = calculate(first, second)
    val rawScore = 1f - msd
    if (rawScore < 0) 0 else rawScore
  }
}
