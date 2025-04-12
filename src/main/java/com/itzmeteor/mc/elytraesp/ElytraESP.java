package com.itzmeteor.mc.elytraesp;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class ElytraESP extends Module {
    private static final String ELYTRA = "minecraft:elytra";

    private final SettingGroup sgGeneral = this.settings.createGroup("General");
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final SettingGroup sgTracers = this.settings.createGroup("Tracers");

    // What to look for
    private final Setting<Boolean> flyingMobsEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("flying-mobs-enabled")
        .description("Look for mobs wearing an elytra.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> elytraItemsEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("elytra-items-enabled")
        .description("Look for elytra items on the ground.")
        .defaultValue(true)
        .build()
    );

    // ESP Settings
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Color of entity's bounding box border.")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    private final Setting<SettingColor> boxColor = sgRender.add(new ColorSetting.Builder()
        .name("box-color")
        .description("Color of entity's bounding box.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    // Tracer Settings
    private final Setting<Boolean> tracersEnabled = sgTracers.add(new BoolSetting.Builder()
        .name("tracers-enabled")
        .description("Enables tracer lines.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgTracers.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("Color of tracer lines.")
        .defaultValue(new SettingColor(255, 0, 0, 150))
        .build()
    );

    private final Set<Entity> scannedEntities = Collections.synchronizedSet(new HashSet<>());
    private int count;

    public ElytraESP() {
        super(Addon.CATEGORY, "elytra-esp", "Bounding Boxes and Tracers to find elytras.");
    }

    @EventHandler
    private void onRender3d(Render3DEvent event) {
        count = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (flyingMobsEnabled.get() && entity instanceof MobEntity mob) {
                mob.getArmorItems().forEach((item) -> {
                    if (item.getItem().toString().equals(ElytraESP.ELYTRA)) {
                        drawBoundingBox(event, entity);

                        if (tracersEnabled.get()) {
                            drawTracer(event, entity);
                        }

                        addEntity(entity);
                        count++;
                    }
                });
            }
            if (elytraItemsEnabled.get() && entity instanceof ItemEntity item) {
                if (item.getStack().getItem().toString().equals(ElytraESP.ELYTRA)) {
                    drawBoundingBox(event, entity);

                    if (tracersEnabled.get()) {
                        drawTracer(event, entity);
                    }

                    addEntity(entity);
                    count++;
                }
            }
        }
    }

    @Override
    public void onActivate() {
        scannedEntities.clear();
    }
    @Override
    public void onDeactivate() {
        scannedEntities.clear();
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }

    private synchronized void drawBoundingBox(Render3DEvent event, Entity entity) {
        double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
        double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
        double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();

        Box box = entity.getBoundingBox();
        event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, boxColor.get(), lineColor.get(), ShapeMode.Both, 0);
    }

    private synchronized void drawTracer(Render3DEvent event, Entity entity) {
        double x = entity.prevX + (entity.getX() - entity.prevX) * event.tickDelta;
        double y = entity.prevY + (entity.getY() - entity.prevY) * event.tickDelta;
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * event.tickDelta;

        event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
            x, y + entity.getHeight() / 2, z, tracerColor.get()
        );
    }

    private synchronized void addEntity(Entity entity) {
        if (!scannedEntities.contains(entity)) {
            scannedEntities.add(entity);

            var coords = ChatUtils.formatCoords(entity.getPos());

            if (entity instanceof MobEntity mob)
            {
                MutableText text = Text.literal("Found a flying ")
                    .append(Text.of(mob.getName().getString()))
                    .append(Text.literal(" at "))
                    .append(Text.of(coords));
                ChatUtils.sendMsg("ElytraESP", text);
            }

            if (entity instanceof ItemEntity)
            {
                MutableText text = Text.literal("Found an elytra item at ")
                    .append(Text.of(coords));
                ChatUtils.sendMsg("ElytraESP", text);
            }
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (mc.world != null){
            Iterable<Entity> entities = mc.world.getEntities();
            Set<Entity> entitySet = new HashSet<>();
            entities.forEach(entity -> entitySet.add(entity));

            scannedEntities.removeIf(entity -> {
                return !entitySet.contains(entity);
            });
        }
    }
}
