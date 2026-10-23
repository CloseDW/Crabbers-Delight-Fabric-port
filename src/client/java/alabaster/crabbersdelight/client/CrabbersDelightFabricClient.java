package alabaster.crabbersdelight.client;

import alabaster.crabbersdelight.CrabbersDelightFabric;
import alabaster.crabbersdelight.common.registry.CDModBlocks;
import alabaster.crabbersdelight.common.registry.CDModEntities;
import alabaster.crabbersdelight.common.entity.crab.CrabModel;
import alabaster.crabbersdelight.common.entity.crab.CrabRenderer;
import alabaster.crabbersdelight.client.gui.CrabTrapGUI;
import alabaster.crabbersdelight.client.gui.TackleBoxGUI;
import alabaster.crabbersdelight.client.renderer.LifePreserverLayer;
import alabaster.crabbersdelight.client.renderer.LifePreserverSeatRenderer;
import alabaster.crabbersdelight.client.renderer.FishingSpotRenderer;
import alabaster.crabbersdelight.client.renderer.FishPlaqueRenderer;
import alabaster.crabbersdelight.client.command.PlaquePoseCommand;
import alabaster.crabbersdelight.client.gui.NoteClientHandler;
import alabaster.crabbersdelight.common.entity.boat.CDBoatModelLayers;
import alabaster.crabbersdelight.common.entity.boat.CDBoatRenderer;
import alabaster.crabbersdelight.common.block.entity.CDSignBlockEntity;
import alabaster.crabbersdelight.common.block.entity.CDHangingSignBlockEntity;
import alabaster.crabbersdelight.common.registry.CDModMenus;
import alabaster.crabbersdelight.common.registry.CDModBlockEntity;
import alabaster.crabbersdelight.common.registry.CDModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import alabaster.crabbersdelight.common.Config;
import alabaster.crabbersdelight.common.item.FishSize;
import alabaster.crabbersdelight.common.utils.FishSizeHelper;
import net.minecraft.text.Text;
import alabaster.crabbersdelight.common.event.LifePreserverEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.render.block.entity.HangingSignBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.ChestBoatEntityModel;

public class CrabbersDelightFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// 蟹钳 GUI 用 2D 模型（手持用 3D），先把 2D 模型注册进烘焙队列
		ModelLoadingPlugin.register(plugin ->
				plugin.addModels(new ModelIdentifier(
						new Identifier("crabbersdelight", "crab_claw_2d"), "inventory")));
		// 椰壳头盔戴在头上（HEAD 视角）时用 3D 模型，先注册进烘焙队列
		ModelLoadingPlugin.register(plugin ->
				plugin.addModels(new ModelIdentifier(
						new Identifier("crabbersdelight", "coconut_helmet_3d"), "inventory")));

		// 贝壳模型有透明镂空，必须注册 Cutout 渲染层
		// （1.20.1 的模型 JSON 不认识 1.21 才有的 render_type 字段）
		BlockRenderLayerMap.INSTANCE.putBlock(CDModBlocks.SEASHELLS, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CDModBlocks.PALM_SAPLING, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CDModBlocks.CRAB_TRAP, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CDModBlocks.TACKLE_BOX, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(CDModBlocks.LIFE_PRESERVER, RenderLayer.getCutout());

		// 螃蟹：渲染器 + 模型层
		EntityRendererRegistry.register(CDModEntities.CRAB, CrabRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(CrabModel.LAYER_LOCATION, CrabModel::createBodyLayer);

		// 捕蟹笼 GUI
		HandledScreens.register(CDModMenus.CRAB_TRAP_MENU, CrabTrapGUI::new);
		// 钓具箱 GUI
		HandledScreens.register(CDModMenus.TACKLE_BOX_MENU, TackleBoxGUI::new);

		// 船：渲染器 + 模型层
		EntityRendererRegistry.register(CDModEntities.MOD_BOAT, ctx -> new CDBoatRenderer(ctx, false));
		EntityRendererRegistry.register(CDModEntities.MOD_CHEST_BOAT, ctx -> new CDBoatRenderer(ctx, true));
		EntityModelLayerRegistry.registerModelLayer(CDBoatModelLayers.PALM_BOAT_LAYER, BoatEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(CDBoatModelLayers.PALM_CHEST_BOAT_LAYER, ChestBoatEntityModel::getTexturedModelData);

		// 救生圈：座位实体渲染器 + 所有生物"穿戴时腰部渲染"的层
		EntityRendererRegistry.register(CDModEntities.LIFE_PRESERVER_SEAT, LifePreserverSeatRenderer::new);
		// 钓点：实体本身不画模型，只靠粒子
		EntityRendererRegistry.register(CDModEntities.FISHING_SPOT, FishingSpotRenderer::new);
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
				(entityType, entityRenderer, registrationHelper, context) ->
						registrationHelper.register(new LifePreserverLayer<>(entityRenderer)));

		// 救生圈浮力：客户端也给本地玩家补一份，避免只靠服务端速度包时手感发飘
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				LifePreserverEvents.applyBuoyancy(client.player);
			}
		});

		// 鱼尺寸：物品提示里追加档位（1.21 用 ItemTooltipEvent，1.20.1 用 Fabric 的 ItemTooltipCallback）
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, lines) -> {
			if (!Config.FISH_SIZE_ENABLED.get()) {
				return;
			}
			FishSize size = FishSizeHelper.get(stack);
			if (size == null) {
				return;
			}
			lines.add(Text.translatable("tooltip.crabbersdelight.fish_size." + size.getSerializedName())
					.formatted(size.getColor()));
		});


		// 鱼牌：把鱼桶里的生物显示在牌子上
		BlockEntityRendererRegistry.register(CDModBlockEntity.FISH_PLAQUE,
				(BlockEntityRendererFactory<alabaster.crabbersdelight.common.block.entity.FishPlaqueBlockEntity>)
						FishPlaqueRenderer::new);

		// 扔出去的瓶中便笺：直接按物品渲染
		EntityRendererRegistry.register(CDModEntities.THROWN_BOTTLED_NOTE, FlyingItemEntityRenderer::new);

		// 便条界面（编辑 / 阅读）
		NoteClientHandler.register();

		// 调试：/cdplaque 实时调整鱼牌上生物的摆放
		PlaquePoseCommand.register();
		// 告示牌渲染器
		BlockEntityRendererRegistry.register(CDModBlockEntity.PALM_SIGN,
				(BlockEntityRendererFactory<SignBlockEntity>) SignBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(CDModBlockEntity.HANGING_PALM_SIGN,
				(BlockEntityRendererFactory<SignBlockEntity>) HangingSignBlockEntityRenderer::new);
	}
}
