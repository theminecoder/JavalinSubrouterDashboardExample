package yolo.test;

import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.javalin.http.Context;
import io.javalin.http.SubRouter;
import me.theminecoder.plugin.Plugin;

@AutoService(Plugin.class)
public class ExamplePlugin implements Plugin {

    @Inject
    private SubRouter router;

    @Override
    public void onEnable() {
        new Thread(() -> {
            System.out.println("Starting long task");
            try {
                Thread.sleep(2000); // Load db connection/intensive task
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Done long task, enabling route");
            router.get("hello", this::sayHello);
        }).start();
    }

    private void sayHello(Context ctx) {
        ctx.json("hello");
    }

    @Override
    public void onDisable() {

    }
}
