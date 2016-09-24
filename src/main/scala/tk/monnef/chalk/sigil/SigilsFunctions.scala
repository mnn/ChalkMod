package tk.monnef.chalk.sigil

import tk.monnef.chalk.core.common._
import net.minecraft.util.EnumFacing
import scalaz._
import Scalaz._
import scala.reflect.ClassTag
import shapeless._
import poly._
import shapeless.syntax.std.tuple._
import shapeless.syntax.std.traversable._
import shapeless.Poly1
import tk.monnef.chalk.sigil.Sigils._

import scala.annotation.tailrec
import scala.reflect.ClassTag

trait SigilsFunctions {
  this: Sigils.type =>

  def formatCanvas(canvas: Canvas): String = canvas.transpose.map(_.mkString(" ")).mkString("\n")

  private[this] def parseByte(input: String): Byte = {
    try {input.toByte}
    catch {
      case e: Throwable =>
        println("[[[\n" + input.toCharArray.toList.map(_.asInstanceOf[Int]) + "\n]]]")
        throw new RuntimeException(s"Cannot parse '$input' as byte.", e)
    }
  }

  def parseCanvas(input: String): Canvas = {
    try {
      input.split("\n").filter(_.trim != "").map(_.split(" +").map(_.trim).map(parseByte)).transpose
    } catch {
      case e: Throwable =>
        println(s"Input: [[$input]]")
        throw new RuntimeException(s"Cannot parse canvas.", e)
    }
  }

  def canvasFromIntArray(intArray: Array[Array[Int]]): Canvas = intArray.map(_.map(_.toByte))

  def computeSigilCenter(canvas: Canvas): (Float, Float) = {
    canvas.zipWith2dIndex.flatten.foldLeft((0f, 0f, 0)) { case (acc@(accX, accY, count), (sigilType, x, y)) =>
      if (sigilType == SigilType.Blank) acc else (accX + x.toFloat, accY + y.toFloat, count + 1)
    } |> { case (x, y, count) => (x / count, y / count) } |> { case (x, y) => (x + .5f, y + .5f) }
  }

  def computeCanvasCenter(canvas: Canvas): (Float, Float) = (canvas.length / 2f, canvas.head.length / 2f)

  object SubtractPair extends Poly1 {
    implicit def caseIntTuple = at[(Int, Int)](x => x._1 - x._2)

    implicit def caseFloatTuple = at[(Float, Float)](x => x._1 - x._2)

    implicit def caseDoubleTuple = at[(Double, Double)](x => x._1 - x._2)
  }

  object AddPair extends Poly1 {
    implicit def caseIntTuple = at[(Int, Int)](x => x._1 + x._2)
  }

  object Round extends Poly1 {
    implicit def caseFloat = at[Float](_.round)
  }

  def centerCanvas(canvas: Canvas): (Canvas, (Int, Int)) = {
    val canvasCenter = computeCanvasCenter(canvas)
    val sigilCenter = computeSigilCenter(canvas)
    // IntelliJ IDEA is wrong - it is actually valid code.
    val shift = canvasCenter.zip(sigilCenter).map(SubtractPair).map(Round)
    (shiftCanvas(canvas, shift), shift)
  }

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

  def computeAllCwRationsWithFacings(canvas: Canvas): List[(Canvas, EnumFacing)] =
    computeAllCwRations(canvas) zip List(EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST)

  def computeAllPossibleShiftsOfOneSize(canvas: Canvas, size: Int = 1): List[(Canvas, (Int, Int))] =
    (for {dir <- EnumFacing.HORIZONTALS} yield {
      val shift = (dir.getFrontOffsetX * size, dir.getFrontOffsetZ * size)
      (shiftCanvas(canvas, shift), shift)
    }).toList

  def computeAllPossibleShifts(canvas: Canvas, maxSize: Int = 1): List[(Canvas, (Int, Int))] =
    List((canvas, (0, 0))) ++ (1 to maxSize).flatMap(computeAllPossibleShiftsOfOneSize(canvas, _))
}
