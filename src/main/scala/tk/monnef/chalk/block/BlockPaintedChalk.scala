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
}

class TilePaintedChalk extends TileEntity with MonnefTileEntity with ITickable {

  import TilePaintedChalk._

  var side = EnumFacing.SOUTH
  private var forceUpdate = false

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
  }

  def writeCustomDataToNBT(tag: NBTTagCompound): NBTTagCompound = {
    val byteIdx = side.getIndex.toByte
    tag.setByte(TagSide, byteIdx)
    tag
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    val nbt = writeCustomDataToNBT(super.writeToNBT(compound))
    println(s"serialized side $side for $getPos on ${FMLCommonHandler.instance().getEffectiveSide}")
    nbt
  }
}

object TilePaintedChalkRenderer {
  private final val Texture = new ResourceLocation(ChalkMod.ModId, "textures/tiles/chalk.png")
}

class TilePaintedChalkRenderer extends TileEntitySpecialRenderer[TilePaintedChalk] {

  import TilePaintedChalkRenderer._

  override def renderTileEntityAt(te: TilePaintedChalk, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(Texture)
    GlStateManager.pushMatrix()
    import EnumFacing._
    val (rotX: Float, rotY: Float, rotZ: Float) = te.side match {
      case DOWN =>
        GlStateManager.color(1, 0, 0, 1)
        (0f, 0f, 0f) // already done
      case UP =>
        GlStateManager.color(0, 1, 0, 1)
        (180f, 0f, 0f)
      case _ =>
        GlStateManager.color(0, 0, 1, 1)
        (90f, 0f, te.side.getHorizontalAngle + 180f)
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
    //    val U = 1
    val U = 1f / (16 / 2)
    buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
    buff.pos(0, 0, U).tex(0, 1).endVertex()
    buff.pos(U, 0, U).tex(1, 1).endVertex()
    buff.pos(U, 0, 0).tex(1, 0).endVertex()
    buff.pos(0, 0, 0).tex(0, 0).endVertex()
    tessellator.draw()
    GlStateManager.popMatrix()
  }
}
