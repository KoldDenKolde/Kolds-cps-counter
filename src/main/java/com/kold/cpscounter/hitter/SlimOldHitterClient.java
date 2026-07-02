package com.kold.cpscounter.hitter;

import com.kold.cpscounter.client.ModState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_746;

public final class SlimOldHitterClient implements ClientModInitializer {
    private static final class_304.class_11900 CATEGORY = class_304.class_11900.method_74698(
        class_2960.method_60655("kolds-cps-counter", "old_hitter")
    );

    private static class_304 hitterKey;
    private static class_304 jumpResetKey;
    private static boolean hitterEnabled = true;
    private static boolean jumpResetEnabled = true;
    private static boolean attackWasDown;
    private static boolean useWasDown;
    private static int lastHurtTime;

    @Override
    public void onInitializeClient() {
        forceDefaults();

        hitterKey = KeyBindingHelper.registerKeyBinding(new class_304(
            "key.kolds-cps-counter.hitter",
            class_3675.class_307.field_1668,
            70,
            CATEGORY
        ));
        jumpResetKey = KeyBindingHelper.registerKeyBinding(new class_304(
            "key.kolds-cps-counter.jump_reset",
            class_3675.class_307.field_1668,
            90,
            CATEGORY
        ));

        ClientTickEvents.START_CLIENT_TICK.register(SlimOldHitterClient::tickEarly);
        ClientTickEvents.END_CLIENT_TICK.register(SlimOldHitterClient::tick);
    }

    private static void tick(class_310 client) {
        handleToggles();
        forceDefaults();
        tickCpsKeys(client);
        tickJumpReset(client);
    }

    private static void tickEarly(class_310 client) {
        tickJumpReset(client);
    }

    private static void forceDefaults() {
        for (int i = 0; i < ModState.TOGGLES.length; i++) {
            ModState.TOGGLES[i] = 0;
        }

        ModState.openModuleMenu = -1;
        ModState.hitterTargetLock = true;
        ModState.jumpResetChance = 100;
        ModState.notificationsEnabled = false;

        setModuleBind(5, 70);
        setModuleEnabled(5, hitterEnabled);
        setModuleBind(6, 90);
        setModuleEnabled(6, jumpResetEnabled);
        setModuleBind(9, 0);
        setModuleEnabled(9, true);

        ModState.widgetsFps = false;
        ModState.widgetsCoords = false;
        ModState.widgetsPing = false;
        ModState.widgetsSpeed = false;
        ModState.widgetsPitch = false;
        ModState.widgetsCps = false;
        ModState.widgetsCompass = false;
        ModState.widgetsBiome = false;
        ModState.cpsHudEnabled = false;
    }

    private static void setModuleBind(int module, int key) {
        int index = module * 2;
        if (index >= 0 && index < ModState.TOGGLES.length) {
            ModState.TOGGLES[index] = key;
        }
    }

    private static void setModuleEnabled(int module, boolean enabled) {
        int index = module * 2 + 1;
        if (index >= 0 && index < ModState.TOGGLES.length) {
            ModState.TOGGLES[index] = enabled ? 1 : 0;
        }
    }

    private static void handleToggles() {
        if (hitterKey != null) {
            while (hitterKey.method_1436()) {
                hitterEnabled = !hitterEnabled;
                if (!hitterEnabled) {
                    ModState.clearHitterTargetLock();
                }
            }
        }
        if (jumpResetKey != null) {
            while (jumpResetKey.method_1436()) {
                jumpResetEnabled = !jumpResetEnabled;
            }
        }
    }

    private static void tickCpsKeys(class_310 client) {
        if (client.field_1690 == null) {
            attackWasDown = false;
            useWasDown = false;
            return;
        }

        boolean attackDown = client.field_1690.field_1886.method_1434();
        boolean useDown = client.field_1690.field_1904.method_1434();
        if (attackDown && !attackWasDown) {
            ModState.recordMouseClick(0);
        }
        if (useDown && !useWasDown) {
            ModState.recordMouseClick(1);
        }
        attackWasDown = attackDown;
        useWasDown = useDown;
    }

    private static void tickJumpReset(class_310 client) {
        class_746 player = client.field_1724;
        if (player == null) {
            lastHurtTime = 0;
            return;
        }

        int hurtTime = player.field_6235;
        boolean newHit = hurtTime > 0 && (lastHurtTime == 0 || hurtTime > lastHurtTime);
        boolean holdingBack = client.field_1690 != null && client.field_1690.field_1881.method_1434();

        if (jumpResetEnabled
            && client.field_1755 == null
            && newHit
            && !holdingBack
            && player.method_24828()) {
            player.method_6043();
        }
        lastHurtTime = hurtTime;
    }
}
