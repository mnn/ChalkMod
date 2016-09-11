package tk.monnef.chalk.sigil

import tk.monnef.chalk.core.common._

import scalaz._
import Scalaz._
import scala.reflect.ClassTag

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
}
