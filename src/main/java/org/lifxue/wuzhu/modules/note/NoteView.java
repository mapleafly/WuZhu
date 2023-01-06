package org.lifxue.wuzhu.modules.note;

import com.dlsc.workbenchfx.Workbench;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.lifxue.wuzhu.modules.note.richtextfx.RichTextView;

/**
  * @classname NoteView
  * @description 记事本View
  * @auhthor lifxue
  * @date 2023/1/6 14:06
  * @version 1.0
*/
public class NoteView extends AnchorPane {
    /**
     * @Description
     * @Author lifxue
     * @Date 2023/1/6 13:42
     * @Param [workbench]
     **/
   public NoteView(Workbench workbench) {
        TabPane tabPane = new TabPane();

        Tab strategyTab = new Tab();
        strategyTab.setText("策略");
        strategyTab.closableProperty().set(false);
        strategyTab.setContent(new RichTextView(workbench, "strategy"));

        Tab noteTab = new Tab("备忘");
        noteTab.closableProperty().set(false);
        noteTab.setContent(new RichTextView(workbench, "note"));

        tabPane.getTabs().addAll(strategyTab, noteTab);

        getChildren().addAll(tabPane);
        setTopAnchor(tabPane, 0.0);
        setLeftAnchor(tabPane, 0.0);
        setRightAnchor(tabPane, 0.0);
        setBottomAnchor(tabPane, 0.0);

    }
}
