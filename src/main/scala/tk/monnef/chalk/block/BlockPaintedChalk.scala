package tk.monnef.chalk.block

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, ITileEntityProvider}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{EnumFaceDirection, GlStateManager, Tessellator}
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{EnumBlockRenderType, EnumFacing, ITickable, ResourceLocation}
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.World
import net.minecraftforge.fml.common.FMLCommonHandler
import org.lwjgl.opengl.GL11
import tk.monnef.chalk.ChalkMod
import tk.monnef.chalk.core.common._
import tk.monnef.chalk.core.{MonnefBlock, MonnefTileEntity}

import scala.util.Random

object BlockPaintedChalk {
  private var disableCollisionBoundingBox = false

  def withDisabledCollisionBox(code: => Unit) {
    disableCollisionBoundingBox = true
    code
    disableCollisionBoundingBox = false
  }
}

class BlockPaintedChalk extends Block(Material.SAND) with MonnefBlock with ITileEntityProvider {
  setupName("paintedChalk")

  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TilePaintedChalk

  @SuppressWarnings(Array("deprecation"))
  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: World, pos: BlockPos): AxisAlignedBB = {
    if (BlockPaintedChalk.disableCollisionBoundingBox) Block.NULL_AABB
    else super.getCollisionBoundingBox(blockState, worldIn, pos)
  }

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.INVISIBLE
}

object TilePaintedChalk {
  private final val TagSide = "chalkSide"
  private final val TagCanvas = "chalkCanvas"
  final val CanvasSize = 8
}

class TilePaintedChalk extends TileEntity with MonnefTileEntity with ITickable {

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

  def paintDot(x: Double, y: Double) {
    paintDot(worldPosToIdx(x), worldPosToIdx(y))
  }

  def paintDot(x: Int, y: Int) {
    println(s"coloring $x $y")
    canvas(x)(y) = true
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

object TilePaintedChalkRenderer {

  import TilePaintedChalk._

  private final val Texture = new ResourceLocation(ChalkMod.ModId, "textures/tiles/chalk.png")
  final val DotSizeInPix = 16 / CanvasSize
  final val DotSize = DotSizeInPix * 1f / 16
}

class TilePaintedChalkRenderer extends TileEntitySpecialRenderer[TilePaintedChalk] {

  import TilePaintedChalkRenderer._
  import TilePaintedChalk._

  override def renderTileEntityAt(te: TilePaintedChalk, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(Texture)
    GlStateManager.pushMatrix()
    import EnumFacing._
    val (rotX: Float, rotY: Float, rotZ: Float) = te.side match {
      case DOWN => (0f, 0f, 0f) // already done
      case UP => (180f, 0f, 0f)
      case _ => (90f, 0f, te.side.getHorizontalAngle + 180f)
    }
    GlStateManager.translate(x, y, z)
    GlStateManager.translate(.5f, .5f, .5f)
    GlStateManager.rotate(rotX, 1, 0, 0)
    GlStateManager.rotate(rotY, 0, 1, 0)
    GlStateManager.rotate(rotZ, 0, 0, 1)
    GlStateManager.translate(-.5f, -.5f, -.5f)
    GlStateManager.translate(0, 0.001f, 0)
    val tessellator = Tessellator.getInstance
    val buff = tessellator.getBuffer
    buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
    val U = DotSize

    val canvas = te.canvas
    for {
      x <- canvas.indices
      y <- canvas.head.indices
      if canvas(x)(y)
    } {
      val sx = x * U
      val sy = y * U
      buff.pos(0 + sx, 0, U + sy).tex(0, 1).endVertex()
      buff.pos(U + sx, 0, U + sy).tex(1, 1).endVertex()
      buff.pos(U + sx, 0, 0 + sy).tex(1, 0).endVertex()
      buff.pos(0 + sx, 0, 0 + sy).tex(0, 0).endVertex()
    }
    tessellator.draw()
    GlStateManager.popMatrix()
  }
}
