package com.bysz;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public final class YGOCardsSearch extends JavaPlugin {
    public static final YGOCardsSearch INSTANCE = new YGOCardsSearch();

    private YGOCardsSearch() {
        super(new JvmPluginDescriptionBuilder("com.bysz.YGOCardsSearch", "1.0.3")
                .name("YGOCardsSearch")
                .author("bysz")
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("YGOCardsSearchPlugin loaded!");
        GlobalEventChannel.INSTANCE.registerListenerHost(new MainFunction());
    }
}