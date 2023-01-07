/*
 * Copyright 2020 lif.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifxue.wuzhu.modules.setting;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
  * @classname PreferencesViewModule
  * @description 首选项设置模块
  * @auhthor lifxue
  * @date 2023/1/6 14:24
  * @version 1.0
*/
@Slf4j
@Component
public class PreferencesViewModule extends WorkbenchModule {

    private FxWeaver fxWeaver;

    public PreferencesViewModule(FxWeaver fxWeaver) {
        super("首选项", MaterialDesign.MDI_SETTINGS);
    }

    @Autowired
    public void setFxWeaver(FxWeaver fxWeaver){
        this.fxWeaver = fxWeaver;
    }

    @Override
    public Node activate() {
        return fxWeaver.loadView(PreferencesViewController.class);
    }
}
