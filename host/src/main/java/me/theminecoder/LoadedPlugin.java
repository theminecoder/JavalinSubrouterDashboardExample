package me.theminecoder;

import com.google.inject.Injector;
import java.net.URLClassLoader;
import me.theminecoder.plugin.Plugin;

public class LoadedPlugin {

    private final PluginInfo info;
    private final URLClassLoader classLoader;
    private final Plugin plugin;
    private final Injector injector;

    public LoadedPlugin(PluginInfo info, URLClassLoader classLoader, Plugin plugin, Injector injector) {
        this.info = info;
        this.classLoader = classLoader;
        this.plugin = plugin;
        this.injector = injector;
    }

    public PluginInfo getInfo() {
        return info;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Injector getInjector() {
        return injector;
    }
}
