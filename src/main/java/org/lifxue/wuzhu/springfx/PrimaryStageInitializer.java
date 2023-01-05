package org.lifxue.wuzhu.springfx;

import com.dlsc.workbenchfx.Workbench;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.lifxue.wuzhu.modules.note.NoteModule;
import org.lifxue.wuzhu.modules.note.NoteView;
import org.lifxue.wuzhu.modules.setting.PreferencesViewModule;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

  private Workbench workbench;

  private NoteModule noteModule;
  private PreferencesViewModule preferencesViewModule;

  public PrimaryStageInitializer(
      Workbench workbench,
      NoteModule noteModule,
      PreferencesViewModule preferencesViewModule
  ){
    this.workbench = workbench;
    this.noteModule = noteModule;
    this.preferencesViewModule = preferencesViewModule;
  }
  @Override
  public void onApplicationEvent(StageReadyEvent event) {
    Stage stage = event.stage;
    stage.setTitle("WuZhu");
    stage.getIcons().add(new Image(Objects.requireNonNull(PrimaryStageInitializer.class.getResource("/org/lifxue/wuzhu/images/lifng.jpg")).toString()));

    workbench.getModules().addAll(
        noteModule,
        preferencesViewModule
    );

    Scene scene = new Scene(workbench);
    stage.setScene(scene);
    stage.setWidth(1000);
    stage.setHeight(700);
    stage.show();
    stage.centerOnScreen();
  }
}
