package org.lifxue.wuzhu.modules.note;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import javafx.scene.Node;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.springframework.stereotype.Component;

/**
  * @classname NoteModule
  * @description 富文本框控件
  * @auhthor lifxue
  * @date 2023/1/6 14:06
  * @version 1.0
*/
@Component
public class NoteModule extends WorkbenchModule {

    /**
     * Super constructor to be called by the implementing class.
     * Uses a MaterialDesign as the icon for this module.
     *
     * @see <a href="https://fontawesome.com/v4.7.0/">FontAwesome v4.7.0 Icons</a>
     */
    public NoteModule() {
        super("记事本", MaterialDesign.MDI_NOTE);
    }


    /**
     * Gets called whenever the currently displayed content is being switched to this module.
     *
     * @return content to be displayed in this module
     * @implNote if a module is being opened from the overview for the first time, it will get
     * initialized first by calling init(), afterwards activate() will be called.
     */
    @Override
    public Node activate() {
        return new NoteView(this.getWorkbench());
    }
}
