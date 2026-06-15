package org.lifxue.wuzhu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "wuzhu.javafx.enabled=false")
class WuZhuApplicationTests {

  @Test
  void contextLoads() {
  }

}
