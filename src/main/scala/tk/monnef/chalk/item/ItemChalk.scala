package tk.monnef.chalk.item

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.math.{BlockPos, RayTraceResult, Vec3d}
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand}
import net.minecraft.world.World
import net.minecraftforge.fml.common.FMLCommonHandler
import tk.monnef.chalk.ChalkMod
import tk.monnef.chalk.ChalkMod.logger
import tk.monnef.chalk.block.{BlockPaintedChalk, ChalkBlocks, TilePaintedChalk}
import tk.monnef.chalk.core.MonnefItem
import tk.monnef.chalk.core.common._

import scala.util.Random

trait ItemBaseChalk

object ItemChalk {
  final val RayTracingRange = 5
}

class ItemChalk extends Item with MonnefItem with ItemBaseChalk {

  import ItemChalk._

  setupName("chalk")
  setMaxStackSize(1)

  def processPlayerDraw(stack: ItemStack, player: EntityPlayer, world: World, pos: BlockPos, hitVec: Vec3d, sideHit: EnumFacing) {
    val chalkBlockPos = pos.offset(sideHit)
    val isReplaceable = world.getBlockState(chalkBlockPos).getBlock.isReplaceable(world, chalkBlockPos)
    if (world.isAirBlock(chalkBlockPos) || isReplaceable) {
      val state = ChalkBlocks.paintedChalk.getDefaultState
      world.setBlockState(chalkBlockPos, state)
      val tile = world.getTileEntity(chalkBlockPos).asInstanceOf[TilePaintedChalk]
      tile.side = sideHit.getOpposite
      world.notifyBlockUpdate(chalkBlockPos, state, state, 3)
    }
    world.getTileEntity(chalkBlockPos).asInstanceOf[TilePaintedChalk] match {
      case null =>
      case te =>
        import EnumFacing._
        val (canvasX: Double, canvasY: Double) = te.side match {
          case DOWN => (hitVec.xCoord, hitVec.zCoord)
          case UP => (-hitVec.xCoord, hitVec.zCoord)
          case NORTH => (hitVec.xCoord, -hitVec.yCoord)
          case SOUTH => (-hitVec.xCoord, -hitVec.yCoord)
          case EAST => (hitVec.zCoord, -hitVec.yCoord)
          case WEST => (-hitVec.zCoord, -hitVec.yCoord)
        }
        te.asInstanceOf[TilePaintedChalk].drawDot(canvasX, canvasY, !player.isSneaking)
      //    world.setBlockState(pos, Seq(Blocks.BEDROCK, Blocks.BOOKSHELF, Blocks.REDSTONE_BLOCK, Blocks.END_STONE).random.getDefaultState)
    }
  }

  override def onItemUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
    if (hand == EnumHand.MAIN_HAND) {
      if (worldIn.getBlockState(pos).getBlock == ChalkBlocks.paintedChalk) {
        val te = worldIn.getTileEntity(pos).asInstanceOf[TilePaintedChalk]
        logger.info(s"chalk block @ $pos has on ${FMLCommonHandler.instance().getEffectiveSide} face ${te.side}")
      }

      if (worldIn.isLogicalServer) {
        // logger.info(s"onItemUse: $pos, $facing, $hitX, $hitY, $hitZ")
        val eyesPos = new Vec3d(playerIn.posX, playerIn.posY + playerIn.getEyeHeight, playerIn.posZ)
        val endVec = eyesPos.add(playerIn.getLookVec.normalize().scale(RayTracingRange))
        var res: RayTraceResult = null
        BlockPaintedChalk.withDisabledCollisionBox {res = worldIn.rayTraceBlocks(eyesPos, endVec, false, true, false)}
        logger.info(s"res: $res")
        if (res != null && res.typeOfHit == RayTraceResult.Type.BLOCK) {
          processPlayerDraw(stack, playerIn, worldIn, res.getBlockPos, res.hitVec, res.sideHit)
        }
      }
      EnumActionResult.SUCCESS
    } else super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ)
  }
}
