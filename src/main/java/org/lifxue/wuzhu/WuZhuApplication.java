package org.lifxue.wuzhu;

import com.dlsc.workbenchfx.Workbench;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.springfx.JavaFxApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
  * @classname WuZhuApplication
  * @description
  * @auhthor lifxue
  * @date 2023/1/6 15:03
  * @version 1.0
*/
@EnableFeignClients
@Slf4j
@SpringBootApplication
public class WuZhuApplication {

    @Bean
    public Workbench workbench() {
        return Workbench.builder().build();
    }

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }

}
