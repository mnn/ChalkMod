package tk.monnef.chalk.sigil

import tk.monnef.chalk.core.common._

import scalaz._
import Scalaz._
import scala.reflect.ClassTag
import Sigils._
import tk.monnef.chalk.sigil.ComparisonImprovement.ComparisonImprovement

import scala.annotation.tailrec

object Sigils {
  type Sigil = Byte
  type Canvas = Array[Array[Sigil]]

  final val CanvasSize = 8

  object SigilType {
    val Blank: Sigil = 0.toByte
    val WhiteChalk: Sigil = 1.toByte
  }

  def computeCanvasCenter(canvas: Canvas): (Float, Float) = {
    canvas.zipWith2dIndex.flatten.foldLeft((0f, 0f, 0)) { case (acc@(accX, accY, count), (sigilType, x, y)) =>
      if (sigilType == SigilType.Blank) acc else (accX + x.toFloat, accY + y.toFloat, count + 1)
    } |> { case (x, y, count) => (x / count, y / count) } |> { case (x, y) => (x + .5f, y + .5f) }
  }

  def formatCanvas(canvas: Canvas): String = canvas.transpose.map(_.mkString(" ")).mkString("\n")

  def parseCanvas(input: String): Canvas = input.split("\n").map(_.split(" ").map(_.toByte)).transpose

  def canvasFromIntArray(intArray: Array[Array[Int]]): Canvas = intArray.map(_.map(_.toByte))

  def shiftArray[T: ClassTag](array: Array[T], offset: Int, emptyValue: T): Array[T] = {
    assert(offset.abs <= array.length)
    if (offset > 0) Array.fill(offset)(emptyValue) ++ array.dropRight(offset)
    else if (offset < 0) array.drop(offset.abs) ++ Array.fill(offset.abs)(emptyValue)
    else array
  }

  def shiftCanvas(canvas: Canvas, offset: (Int, Int)): Canvas = {
    val (offX, offY) = offset
    shiftArray(canvas: Array[Array[Sigil]], offX, Array.fill(canvas.head.length)(SigilType.Blank))
    .map { col: Array[Sigil] => shiftArray(col, offY, SigilType.Blank) }
  }

  def rotateCanvasCw(canvas: Canvas): Canvas = canvas.map(_.reverse).transpose

  def computeAllCwRations(canvas: Canvas): List[Canvas] = {
    @tailrec def loop(last: Canvas, rem: Int, res: List[Canvas]): List[Canvas] = rem match {
      case 0 => res
      case n =>
        val newLast = rotateCanvasCw(last)
        loop(newLast, rem - 1, res ++ List(newLast))
    }
    loop(canvas, 3, List(canvas))
  }
}

object ComparisonImprovement extends Enumeration {
  type ComparisonImprovement = Value
  val Min, Max = Value
}

trait ComparisonMethod {
  def calculate(first: Canvas, second: Canvas): Float

  def improvement: ComparisonImprovement
}

object MeanSquareDifference extends ComparisonMethod {
  def calculate(first: Canvas, second: Canvas): Float = {
    val size = first.length * first.head.length
    // possibly very inefficient, rewrite uglier?
    val differences = first.flatten.toList.zip(second.flatten.toList)
                      .map { case (f: Sigil, s: Sigil) => Math.pow(f.toFloat - s, 2) }
    val sum = differences.sum
    sum.toFloat / size
  }

  override def improvement: ComparisonImprovement = ComparisonImprovement.Min
}

trait SigilComparisonResult {
  /**
    * @return 0-1 where 1 is identity and 0 no similarity at all.
    */
  def score: Float
}

trait SigilComparator {
  def calculateSimilarity: SigilComparisonResult
}

case class CenteringRotatingSlightlyShiftingSigilComparatorResult(score: Float) extends SigilComparisonResult

object CenteringRotatingSlightlyShiftingSigilComparator extends SigilComparator {
  override def calculateSimilarity: SigilComparisonResult = {
    // TODO
    CenteringRotatingSlightlyShiftingSigilComparatorResult(-1)
  }
}
