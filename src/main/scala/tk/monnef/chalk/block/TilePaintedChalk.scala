package tk.monnef.chalk.block

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{EnumFacing, ITickable}
import net.minecraftforge.fml.common.FMLCommonHandler
import tk.monnef.chalk.ChalkMod
import tk.monnef.chalk.core.MonnefTileEntity
import tk.monnef.chalk.core.common._
import tk.monnef.chalk.item.ItemBaseChalk

object TilePaintedChalk {
  private final val TagSide = "chalkSide"
  private final val TagCanvas = "chalkCanvas"
  final val CanvasSize = 8
}

class TilePaintedChalk extends TileEntity with MonnefTileEntity with ITickable {
  def blockActivated(player: EntityPlayer, heldItem: ItemStack): Boolean = {
    if (worldObj.isLogicalServer) {
      for {
        x <- canvas.indices
        col = canvas(x)
        y <- col.indices
        cell = col(y)
      } {
      }

      canvas.transpose.foreach { row => println(row.map {if (_) "1" else "0"}.mkString("")) }

      //if (Option(heldItem).flatMap(x => Option(x.getItem)).exists(_.isInstanceOf[ItemBaseChalk])) {    }
      if (heldItem == null) {
        println("activation")
        true
      } else false
    } else false
  }


  import TilePaintedChalk._

  var side = EnumFacing.SOUTH
  private var forceUpdate = false
  var canvas: Array[Array[Boolean]] = Array.fill(CanvasSize, CanvasSize)(false)

  //  canvas = Array.tabulate(CanvasSize, CanvasSize)((_, _) => Random.nextBoolean())

  canvas(0)(0) = true
  canvas(1)(0) = true
  canvas(2)(0) = false
  canvas(0)(1) = true
  canvas(1)(1) = false
  canvas(2)(1) = false
  canvas(0)(2) = true
  canvas(1)(2) = false
  canvas(2)(2) = false
  canvas(0)(3) = false
  canvas(1)(3) = false
  canvas(2)(3) = false

  private def worldPosToIdx(p: Double): Int = {
    val mod = p % 1f
    val fixed = if (mod < 0) mod + 1 else mod
    (fixed * CanvasSize).floor.toInt
  }

  def drawDot(x: Double, y: Double, drawing: Boolean) {
    drawDot(worldPosToIdx(x), worldPosToIdx(y), drawing)
  }

  def drawDot(x: Int, y: Int, drawing: Boolean) {
    println(s"coloring $x $y")
    canvas(x)(y) = drawing
    forceResynchronization()
  }

  override def update(): Unit = {
    if (worldObj.isLogicalClient) {
      if (forceUpdate) {
        val state = worldObj.getBlockState(pos)
        worldObj.notifyBlockUpdate(pos, state, state, 3)
        worldObj.markChunkDirty(pos, this)
        markDirty()
        forceUpdate = false
        markDirty()
      }
    }
    if (worldObj.isLogicalClient) {
    }
  }

  override def getUpdateTag: NBTTagCompound = {
    val tag = super.getUpdateTag
    writeCustomDataToNBT(tag)
  }

  override def getUpdatePacket: SPacketUpdateTileEntity = {
    new SPacketUpdateTileEntity(pos, 0, getUpdateTag())
  }

  override def onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity): Unit = {
    deserializeNBT(pkt.getNbtCompound)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    if (!compound.hasKey(TagSide)) {
      ChalkMod.logger.error(s"TE @ $pos is missing $TagSide!")
      forceUpdate = true
    } else {
      val sideIndex = compound.getByte(TagSide)
      side = EnumFacing.values()(sideIndex)
      println(s"deserialized side $side ($sideIndex) for $getPos on ${FMLCommonHandler.instance().getEffectiveSide}")
    }
    if (compound.hasKey(TagCanvas)) {
      canvas = compound.getByteArray(TagCanvas).map { case 0 => false case 1 => true }.grouped(CanvasSize).toArray
      println(s"deserialized canvas for $getPos on ${FMLCommonHandler.instance().getEffectiveSide}: ${canvas.map(_.map { case true => 1 case false => 0 }.mkString("")).mkString(" ")}.")
    }
  }

  def writeCustomDataToNBT(tag: NBTTagCompound): NBTTagCompound = {
    val byteIdx = side.getIndex.toByte
    tag.setByte(TagSide, byteIdx)
    //noinspection MapFlatten
    tag.setByteArray(TagCanvas, canvas.map(_.map(x => if (x) 1.toByte else 0.toByte)).flatten)
    tag
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    val nbt = writeCustomDataToNBT(super.writeToNBT(compound))
    println(s"serialized side $side for $getPos on ${FMLCommonHandler.instance().getEffectiveSide}")
    nbt
  }
}