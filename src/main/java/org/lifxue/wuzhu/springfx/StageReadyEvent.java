package org.lifxue.wuzhu.springfx;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

/**
  * @classname StageReadyEvent
  * @description 定义事件
  * @auhthor lifxue
  * @date 2023/1/6 14:07
  * @version 1.0
*/
public class StageReadyEvent extends ApplicationEvent {

    public final Stage stage;

    public StageReadyEvent(Stage stage) {
        super(stage);
        this.stage = stage;
    }
}
