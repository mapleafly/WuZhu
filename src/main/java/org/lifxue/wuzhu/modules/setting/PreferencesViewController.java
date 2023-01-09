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

import com.dlsc.workbenchfx.Workbench;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.enums.BooleanEnum;
import org.lifxue.wuzhu.enums.ThemeEnum;
import org.lifxue.wuzhu.themes.InterfaceTheme;
import org.lifxue.wuzhu.util.PrefsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @version 1.0
 * @classname PreferencesViewController
 * @description 首选项设置模块控制器
 * @auhthor lifxue
 * @date 2023/1/6 14:25
 */
@Slf4j
@Component
@FxmlView("PreferencesView.fxml")
public class PreferencesViewController implements Initializable {

    private final int PRICE_MIN = 50;
    private final int PRICE_MAX = 5000;

    @FXML
    private RadioButton lightRadio;
    @FXML
    private ToggleGroup modeGroup;
    @FXML
    private RadioButton nightRadio;
    @FXML
    private CheckBox autoPriceCheck;
    @FXML
    private CheckBox autoCoinInfoCheck;
    @FXML
    private CheckBox notSmallCheck;
    @FXML
    private Spinner<Integer> numSpinner;
    @FXML
    private TextField portTextField;
    @FXML
    private TextField hostTextField;
    @FXML
    private CheckBox proxyCheck;
    @FXML
    private TextField apikeyTextField;

    private Workbench workbench;
    private InterfaceTheme interfaceTheme;

    @Autowired
    public PreferencesViewController(Workbench workbench, InterfaceTheme interfaceTheme) {
        this.workbench = workbench;
        this.interfaceTheme = interfaceTheme;
    }

    /**
     * @description Initializes the controller class.
     * @author lifxue
     * @date 2023/1/6 14:29
     **/
    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        //初始化界面主题
        lightRadio.setUserData(ThemeEnum.LIGHT);
        nightRadio.setUserData(ThemeEnum.NIGHT);
        String themeValue =
            PrefsHelper.getPreferencesValue(PrefsHelper.THEME, ThemeEnum.LIGHT.toString());
        ThemeEnum themeEnum = ThemeEnum.valueOf(themeValue);
        switch (themeEnum) {
            case NIGHT:
                nightRadio.setSelected(true);
                nightRadio.requestFocus();
                break;
            case LIGHT:
            default:
                lightRadio.setSelected(true);
                lightRadio.requestFocus();
                break;
        }
        //初始化自动更新设置
        String apValue =
            PrefsHelper.getPreferencesValue(PrefsHelper.UPDATEPRICE, BooleanEnum.NO.toString());
        BooleanEnum apEnum = BooleanEnum.valueOf(apValue);
        autoPriceCheck.setSelected(apEnum.equals(BooleanEnum.YES));

        String acValue =
            PrefsHelper.getPreferencesValue(PrefsHelper.COINIDMAP, BooleanEnum.NO.toString());
        BooleanEnum acEnum = BooleanEnum.valueOf(acValue);
        autoCoinInfoCheck.setSelected(acEnum.equals(BooleanEnum.YES));

        //初始化小额品种设置
        SpinnerValueFactory<Integer> spinner =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(PRICE_MIN, PRICE_MAX, 100, 100);
        numSpinner.setValueFactory(spinner);

        String notSmallCoinValue =
            PrefsHelper.getPreferencesValue(PrefsHelper.NOTSMALLCOIN, BooleanEnum.NO.toString());
        BooleanEnum notSmallCoinEnum = BooleanEnum.valueOf(notSmallCoinValue);
        if (notSmallCoinEnum.equals(BooleanEnum.YES)) {
            notSmallCheck.setSelected(true);
            numSpinner.setDisable(false);
            String notSmallCoinNumValue =
                PrefsHelper.getPreferencesValue(PrefsHelper.NOTSMALLCOINNUM, "100");
            numSpinner.getValueFactory().setValue(Integer.valueOf(notSmallCoinNumValue));
        } else {
            notSmallCheck.setSelected(false);
            numSpinner.setDisable(true);
        }
        //初始化代理设置
        String proxyValue = PrefsHelper.getPreferencesValue(PrefsHelper.PROXY, BooleanEnum.NO.toString());
        BooleanEnum proxyEnum = BooleanEnum.valueOf(proxyValue);
        if (proxyEnum.equals(BooleanEnum.YES)) {
            proxyCheck.setSelected(true);
            hostTextField.setDisable(false);
            hostTextField.setText(PrefsHelper.getPreferencesValue(PrefsHelper.HOST, "127.0.0.1"));
            portTextField.setDisable(false);
            portTextField.setText(PrefsHelper.getPreferencesValue(PrefsHelper.PORT, "56908"));
        } else {
            proxyCheck.setSelected(false);
            hostTextField.setDisable(true);
            hostTextField.setText(PrefsHelper.getPreferencesValue(PrefsHelper.HOST, "127.0.0.1"));
            portTextField.setDisable(true);
            portTextField.setText(PrefsHelper.getPreferencesValue(PrefsHelper.PORT, "56908"));
        }

        //初始化coinmarketcap.com参数
        apikeyTextField.setText(PrefsHelper.getPreferencesValue(PrefsHelper.CMC_API_KEY, ""));
    }

    /**
     * @param event
     * @return void
     * @description 保存按钮操作
     * @author lifxue
     * @date 2023/1/6 14:31
     **/
    @FXML
    private void handleSave(ActionEvent event) {
        // theme
        String ra = modeGroup.getSelectedToggle().getUserData().toString();
        PrefsHelper.updatePreferencesValue(PrefsHelper.THEME, ra);

        // db
        if (autoPriceCheck.isSelected()) {
            PrefsHelper.updatePreferencesValue(PrefsHelper.UPDATEPRICE, BooleanEnum.YES.toString());
        } else {
            PrefsHelper.updatePreferencesValue(PrefsHelper.UPDATEPRICE, BooleanEnum.NO.toString());
        }
        if (autoCoinInfoCheck.isSelected()) {
            PrefsHelper.updatePreferencesValue(PrefsHelper.COINIDMAP, BooleanEnum.YES.toString());
        } else {
            PrefsHelper.updatePreferencesValue(PrefsHelper.COINIDMAP, BooleanEnum.NO.toString());
        }
        // 品种比例图设置
        if (notSmallCheck.isSelected()) {
            PrefsHelper.updatePreferencesValue(PrefsHelper.NOTSMALLCOIN, BooleanEnum.YES.toString());
            PrefsHelper.updatePreferencesValue(
                PrefsHelper.NOTSMALLCOINNUM, numSpinner.getValue().toString());
        } else {
            PrefsHelper.updatePreferencesValue(PrefsHelper.NOTSMALLCOIN, BooleanEnum.NO.toString());
        }
        //代理设置
        if (proxyCheck.isSelected()) {
            PrefsHelper.updatePreferencesValue(PrefsHelper.PROXY, BooleanEnum.YES.toString());
            PrefsHelper.updatePreferencesValue(PrefsHelper.HOST, hostTextField.getText());
            PrefsHelper.updatePreferencesValue(PrefsHelper.PORT, portTextField.getText());
        } else {
            PrefsHelper.updatePreferencesValue(PrefsHelper.PROXY, BooleanEnum.NO.toString());
        }
        //保存apikey
        PrefsHelper.updatePreferencesValue(PrefsHelper.CMC_API_KEY, apikeyTextField.getText());

        // 刷新保存
        PrefsHelper.flushPreferences();

        // theme即时生效
        //InterfaceTheme theme = new InterfaceTheme(workbench);
        interfaceTheme.initNightMode();

        workbench.showInformationDialog("消息", "设置信息保存成功！", buttonType -> {
        });
    }

    /**
     * @param event
     * @description 初始化数据库按钮操作
     * @author lifxue
     * @date 2023/1/6 14:32
     **/
    @FXML
    private void handleInitDB(ActionEvent event) {
        //InitTable.dropTable();
        //InitTable.createTable();
        //CoinInfo info = new CoinInfo(workbench,null);
        //if (info.updateCoinIDMap()) {
        //    workbench.showInformationDialog("消息", "初始化数据库成功！", buttonType -> {
        //    });
        //} else {
        //    workbench.showInformationDialog("消息", "初始化数据库失败！", buttonType -> {
        //    });
        //}
    }

    /**
     * @param event
     * @return void
     * @description 忽略小额品种复选框操作
     * @author lifxue
     * @date 2023/1/6 14:48
     **/
    @FXML
    private void handleNotSmallCheckOnAction(ActionEvent event) {
        numSpinner.setDisable(!notSmallCheck.isSelected());
    }

    /**
     * @param actionEvent
     * @return void
     * @description 代理设置复选框操作
     * @author lifxue
     * @date 2023/1/6 14:47
     **/
    @FXML
    public void proxyCheckOnAction(ActionEvent actionEvent) {
        hostTextField.setDisable(!proxyCheck.isSelected());
        portTextField.setDisable(!proxyCheck.isSelected());
    }
}
