package me.theminecoder;

import com.google.inject.AbstractModule;
import io.javalin.http.Router;
import io.javalin.http.SubRouter;

public class PluginGuiceModule extends AbstractModule {

    private SubRouter router;

    public PluginGuiceModule(SubRouter router) {
        this.router = router;
    }

    @Override
    protected void configure() {
        bind(Router.class).toInstance(router);
        bind(SubRouter.class).toInstance(router);
    }
}
