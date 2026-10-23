package alabaster.crabbersdelight.client.renderer;

import alabaster.crabbersdelight.common.entity.LifePreserverSeat;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

// 救生圈座位不画任何东西（对应 1.21 版 LifePreserverSeatRenderer）
public class LifePreserverSeatRenderer extends EntityRenderer<LifePreserverSeat> {
    private static final Identifier UNUSED_TEXTURE =
            new Identifier("crabbersdelight", "textures/entity/life_preserver_seat_unused.png");

    public LifePreserverSeatRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(LifePreserverSeat entity) {
        return UNUSED_TEXTURE;
    }
}
