package tk.monnef.chalk

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import org.apache.logging.log4j.Logger
import tk.monnef.chalk.block.{ChalkBlocks, TilePaintedChalk, TilePaintedChalkRenderer}
import tk.monnef.chalk.item.ChalkItems

@Mod(modid = ChalkMod.ModId, version = ChalkMod.Version, name = ChalkMod.ModName, modLanguage = "scala")
object ChalkMod {
  final val ModId = "chalk"
  final val ModName = "Chalk"
  final val Version = "0.1.0"
  final var logger: Logger = _

  final val creativeTab = new CreativeTabs(ModId) {
    override def getTabIconItem: Item = ChalkItems.chalk
  }

  @SidedProxy(clientSide = "tk.monnef.chalk.ClientOnlyProxy", serverSide = "tk.monnef.chalk.CommonProxy")
  var proxy: CommonProxy = null

  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    logger = event.getModLog
    logger.info(s"Mod _${ModName}_ by monnef is initializing...")
    proxy.preInit()
  }

  @EventHandler
  def init(e: FMLInitializationEvent): Unit = {
    proxy.init()
  }

  @EventHandler
  def init(e: FMLPostInitializationEvent): Unit = {
    proxy.postInit()
    logger.info(s"Mod _${ModName}_ finished initialization.")
  }
}

class CommonProxy {
  def postInit(): Unit = {}

  def preInit(): Unit = {
    ChalkItems.registerItems()
    ChalkBlocks.registerBlocks()
  }

  def init(): Unit = {
    ChalkItems.registerRecipes()
  }
}

class ClientOnlyProxy extends CommonProxy {
  override def preInit(): Unit = {
    super.preInit()
  }

  override def init(): Unit = {
    super.init()
    ChalkItems.registerItemModels()
    registerTileRenders()
  }

  override def postInit(): Unit = {
    super.postInit()
  }

  private[this] def registerRenderer[T <: TileEntity](teCls: Class[T], renderer: TileEntitySpecialRenderer[T]) = {
    ClientRegistry.bindTileEntitySpecialRenderer(teCls, renderer)
  }

  def registerTileRenders(): Unit = {
    registerRenderer(classOf[TilePaintedChalk], new TilePaintedChalkRenderer)
  }

}
