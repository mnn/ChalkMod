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

import scala.annotation.tailrec

object Sigils extends SigilsFunctions {
  type Sigil = Byte
  type Canvas = Array[Array[Sigil]]

  final val CanvasSize = 8

  object SigilType {
    val Blank: Sigil = 0.toByte
    val WhiteChalk: Sigil = 1.toByte
  }
}
