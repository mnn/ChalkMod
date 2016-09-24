package tk.monnef.chalk.sigil

import net.minecraft.util.EnumFacing
import tk.monnef.chalk.core.common._
import scalaz._
import Scalaz._
import scala.reflect.ClassTag
import shapeless._
import poly._
import shapeless.syntax.std.tuple._
import shapeless.syntax.std.traversable._

import Sigils._

trait SigilComparator {
  def calculateSimilarity(input: Canvas, template: Canvas): SigilComparisonResult
}

trait SigilComparisonResult {
  /**
    * @return 0-1 where 1 is identity and 0 no similarity at all.
    */
  def score: Float

  /**
    * Get direction in which template is matched.
    *
    * Base case is canvas on floor with drawn up arrow which corresponds to north. Match is computed clock-wise,
    * first best match is returned.
    */
  def facing: EnumFacing

  def shift: (Int, Int)
}


case class CenteringRotatingSlightlyShiftingSigilComparatorResult(
                                                                   score: Float,
                                                                   facing: EnumFacing,
                                                                   shift: (Int, Int)
                                                                 ) extends SigilComparisonResult

object CenteringRotatingSlightlyShiftingSigilComparator extends SigilComparator {
  private[this] val dirs = List(EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.EAST)

  private[this] case class SolutionDescription(shift: (Int, Int), cwRotations: Int)

  override def calculateSimilarity(input: Canvas, template: Canvas): SigilComparisonResult = calculateSimilarity(input, template, 1)

  def calculateSimilarity(input: Canvas, template: Canvas, maxShiftSize: Int): SigilComparisonResult = {
    // TODO: rewrite to use stream for fast fail?
    val (centeredInput, centerShift) = centerCanvas(input)
    val results = for {
      (shiftedCanvas, shift@(shiftX, shiftY)) <- computeAllPossibleShifts(centeredInput, maxShiftSize)
      (shiftedRotatedCanvas, facing) <- computeAllCwRationsWithFacings(shiftedCanvas)
    } yield {
      val score = MeanSquareDifference.calculateScore(shiftedRotatedCanvas, template)
      val totalShift: (Int, Int) = shift.zip(centerShift).map(AddPair) // IntelliJ IDEA is wrong here, ignore it
      CenteringRotatingSlightlyShiftingSigilComparatorResult(score, facing, totalShift)
    }

    //    println(results.mkString("\n"))
    results.sortBy(1 - _.score).head
  }
}
