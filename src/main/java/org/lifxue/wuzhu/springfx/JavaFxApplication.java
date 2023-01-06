package org.lifxue.wuzhu.springfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.lifxue.wuzhu.WuZhuApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
  * @classname JavaFxApplication
  * @description 加载spring，发布事件，事件里包裹Stage
  * @auhthor lifxue
  * @date 2023/1/6 14:12
  * @version 1.0
*/
public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        this.context = new SpringApplicationBuilder()
            .sources(WuZhuApplication.class)
            .run(getParameters().getRaw().toArray(new String[0]));
    }

    /**
     * @description
     * @author lifxue
     * @date 2023/1/6 14:08
     * @param stage
     **/
    @Override
    public void start(Stage stage) throws Exception {
        //发布事件，其他的交给PrimaryStageInitializer来监听
        context.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() throws Exception {
        context.close();
        Platform.exit();
    }
}
