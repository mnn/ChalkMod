package tk.monnef.chalk.block

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, ITileEntityProvider}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{EnumBlockRenderType, EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}
import tk.monnef.chalk.core.MonnefBlock

object BlockPaintedChalk {
  final val PaintHeight = 1f / 16 * .5f

  final val AabbDown = new AxisAlignedBB(0, 0, 0, 1, PaintHeight, 1)
  final val AabbUp = new AxisAlignedBB(0, 1, 0, 1, 1 - PaintHeight, 1)

  final val AabbNorth = new AxisAlignedBB(0, 0, 0, 1, 1, PaintHeight)
  final val AabbSouth = new AxisAlignedBB(0, 0, 1, 1, 1, 1 - PaintHeight)

  final val AabbEast = new AxisAlignedBB(1, 0, 0, 1 - PaintHeight, 1, 1)
  final val AabbWest = new AxisAlignedBB(0, 0, 0, PaintHeight, 1, 1)

  private var disableCollisionBoundingBox = false

  def withDisabledCollisionBox(code: => Unit) {
    disableCollisionBoundingBox = true
    code
    disableCollisionBoundingBox = false
  }
}

class BlockPaintedChalk extends Block(Material.SAND) with MonnefBlock with ITileEntityProvider {

  import BlockPaintedChalk._

  setupName("paintedChalk")
  setHardness(.5f)

  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TilePaintedChalk

  def getBoxFromSide(facing: EnumFacing): AxisAlignedBB = {
    import EnumFacing._
    facing match {
      case DOWN => AabbDown
      case UP => AabbUp
      case EAST => AabbEast
      case WEST => AabbWest
      case NORTH => AabbNorth
      case SOUTH => AabbSouth
    }
  }

  @SuppressWarnings(Array("deprecation"))
  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: World, pos: BlockPos): AxisAlignedBB = {
    if (BlockPaintedChalk.disableCollisionBoundingBox) Block.NULL_AABB
    else {
      val te = worldIn.getTileEntity(pos).asInstanceOf[TilePaintedChalk]
      if (te == null) Block.NULL_AABB
      else getBoxFromSide(te.side)
    }
  }

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isFullCube(state: IBlockState): Boolean = false

  override def getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.INVISIBLE

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = {
    source.getTileEntity(pos) match {
      case te: TilePaintedChalk => getBoxFromSide(te.side)
      case _ => Block.NULL_AABB
    }
  }

  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
                                hand: EnumHand, heldItem: ItemStack, side: EnumFacing,
                                hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    Option(worldIn.getTileEntity(pos).asInstanceOf[TilePaintedChalk]).exists(_.blockActivated(playerIn, heldItem))
  }
}
