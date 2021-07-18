package me.theminecoder;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;
import io.javalin.core.event.HandlerMetaInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import me.theminecoder.plugin.Plugin;

public class DashboardHost {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector();
        Gson gson = new Gson();

        var handlers = new ArrayList<HandlerMetaInfo>();

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        })
                .get(ctx -> ctx.json(handlers))
                .events(listener -> listener.handlerAdded(handler -> {
                    handlers.add(handler);
                    System.out.println("Route Added: " + handler.getHttpMethod() + " " + handler.getPath() + " " + handler.getRoles() + " -> " + handler.getHandler());
                }));

        var pluginFolder = new File("plugins");
        if (pluginFolder.exists() && !pluginFolder.isDirectory())
            throw new RuntimeException("plugins not a directory!");
        if (!pluginFolder.exists()) pluginFolder.mkdirs();

        var plugins = new ArrayList<LoadedPlugin>();
        System.out.println("Searching for plugins...");
        for (File plugin : Objects.requireNonNull(pluginFolder.listFiles(pathname -> pathname.getName().toLowerCase(Locale.ROOT).endsWith(".jar")))) {
            System.out.println("Checking " + plugin);
            URLClassLoader pluginClassLoader = URLClassLoader.newInstance(new URL[]{plugin.toURI().toURL()});
            PluginInfo info;
            try (InputStream pluginInfoStream = Objects.requireNonNull(pluginClassLoader.getResourceAsStream("plugin.json"))) {
                info = gson.fromJson(new InputStreamReader(pluginInfoStream), PluginInfo.class);
            } catch (NullPointerException e) {
                System.err.println(plugin + " is not a valid plugin!");
                continue;
            }
            System.out.println("Loading plugin " + info.getId() + ": " + info.getName());
            Injector pluginInjector = injector.createChildInjector(new PluginGuiceModule(app.path(info.getId())));
            for (Plugin pluginInstance : GuiceServiceLoader.load(Plugin.class, pluginInjector, pluginClassLoader)) {
                plugins.add(new LoadedPlugin(info, pluginClassLoader, pluginInstance, pluginInjector));
                break;
            }
        }

        plugins.stream()
                .peek(plugin -> System.out.println("Enabling " + plugin.getInfo().getId() + ": " + plugin.getInfo().getName()))
                .map(LoadedPlugin::getPlugin)
                .forEach(Plugin::onEnable);

        app.start(3000);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
            plugins.stream()
                    .peek(plugin -> System.out.println("Disabling " + plugin.getInfo().getId() + ": " + plugin.getInfo().getName()))
                    .map(LoadedPlugin::getPlugin)
                    .forEach(Plugin::onDisable);
        }, "Shutdown"));
    }

}
