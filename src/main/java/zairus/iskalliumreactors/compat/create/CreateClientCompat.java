package zairus.iskalliumreactors.compat.create;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import zairus.iskalliumreactors.IRConstants;
import zairus.iskalliumreactors.compat.ModCompat;

/**
 * Client-only Create/Flywheel registration - mirrors zairus.iskalliumreactors.client.ClientSetup's
 * existing @EventBusSubscriber(Dist.CLIENT) pattern for the same "never touch this on a
 * dedicated server" guarantee, plus an explicit ModCompat.CREATE_LOADED check since that
 * annotation only gates client-vs-server, not whether Create itself is present.
 *
 * Two independent registrations happen here, matching how every one of Create's own kinetic
 * blocks is set up (see e.g. AllBlockEntityTypes' `.visual(...).renderer(...)` pairs):
 *  - The Flywheel visual (PowerTapVisual) - GPU-instanced, used whenever Flywheel
 *    visualization is supported for the level. This is the path that actually renders in
 *    the normal case.
 *  - The vanilla BlockEntityRenderer (PowerTapCreateRenderer) - CPU-drawn fallback for
 *    when Flywheel isn't active (e.g. its backend is OFF). PowerTapCreateRenderer no-ops
 *    itself whenever Flywheel is handling it, so registering both is always safe.
 *
 * Create's own Registrate-based blocks register their visual during FMLClientSetupEvent
 * (see CreateBlockEntityBuilder#registerVisualizer) - matched here for the same reason:
 * VisualizerRegistry needs the block entity type to exist first, and FMLClientSetupEvent is
 * the conventional point every mod (including Create itself) does this kind of client-only
 * wiring.
 */
@EventBusSubscriber(modid = IRConstants.MOD_ID, value = Dist.CLIENT)
public class CreateClientCompat
{
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        if (!ModCompat.CREATE_LOADED)
            return;

        event.registerBlockEntityRenderer(CreateCompat.IR_POWER_TAP_CREATE.get(), PowerTapCreateRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        if (!ModCompat.CREATE_LOADED)
            return;

        // enqueueWork isn't required here (SimpleBlockEntityVisualizer/VisualizerRegistry
        // are just registering into a map, not touching GL state), but keeping registration
        // on the main thread on general principle costs nothing.
        event.enqueueWork(() -> SimpleBlockEntityVisualizer.builder(CreateCompat.IR_POWER_TAP_CREATE.get())
                .factory(PowerTapVisual::new)
                .apply());
    }
}
