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
package org.lifxue.wuzhu.modules.selectcoin;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lif
 */
@Slf4j
@Component
public class SelectCoinViewModule extends WorkbenchModule {
    private FxWeaver fxWeaver;

    public SelectCoinViewModule() {
        super("币种管理", MaterialDesign.MDI_SELECT);
    }

    @Autowired
    public void setFxWeaver(FxWeaver fxWeaver){
        this.fxWeaver = fxWeaver;
    }

    @Override
    public Node activate() {
        //AnchorPane view = null;
        //try {
        //    FXMLLoader loader = new FXMLLoader();
        //    loader.setLocation(SelectCoinModule.class.getResource("SelectCoinView.fxml"));
        //    view = loader.load();
        //
        //    SelectCoinViewController controller = loader.getController();
        //    controller.setWorkbench(getWorkbench());
        //
        //} catch (IOException e) {
        //    log.error(e.toString());
        //}
        //return view;

        return fxWeaver.loadView(SelectCoinViewController.class);
    }
}
