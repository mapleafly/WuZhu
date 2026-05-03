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
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.constant.AppConstants;
import org.lifxue.wuzhu.enums.BooleanEnum;
import org.lifxue.wuzhu.enums.ThemeEnum;
import org.lifxue.wuzhu.service.DatabaseBackupService;
import org.lifxue.wuzhu.themes.InterfaceTheme;
import org.lifxue.wuzhu.util.PrefsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
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
    @FXML
    private Label backupStatusLabel;

    private Workbench workbench;
    private InterfaceTheme interfaceTheme;

    private final DatabaseBackupService databaseBackupService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    public PreferencesViewController(
        Workbench workbench,
        InterfaceTheme interfaceTheme,
        DatabaseBackupService databaseBackupService
    ) {
        this.workbench = workbench;
        this.interfaceTheme = interfaceTheme;
        this.databaseBackupService = databaseBackupService;
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
            hostTextField.setText(PrefsHelper.getPreferencesValue(PrefsHelper.HOST, AppConstants.DEFAULT_PROXY_HOST));
            portTextField.setDisable(false);
            portTextField.setText(PrefsHelper.getPreferencesValue(PrefsHelper.PORT, AppConstants.DEFAULT_PROXY_PORT));
        } else {
            proxyCheck.setSelected(false);
            hostTextField.setDisable(true);
            hostTextField.setText(PrefsHelper.getPreferencesValue(PrefsHelper.HOST, AppConstants.DEFAULT_PROXY_HOST));
            portTextField.setDisable(true);
            portTextField.setText(PrefsHelper.getPreferencesValue(PrefsHelper.PORT, AppConstants.DEFAULT_PROXY_PORT));
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

        workbench.showInformationDialog("消息", "设置信息保存成功, 请重启软件使设置生效！", buttonType -> {
        });
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

    @FXML
    private void handleBackup(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择备份文件保存位置");
            fileChooser.setInitialFileName("wuzhu_backup_" + System.currentTimeMillis() + ".sql");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SQL文件", "*.sql"),
                new FileChooser.ExtensionFilter("ZIP文件", "*.zip")
            );

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                String backupPath;
                if (file.getName().endsWith(".zip")) {
                    backupPath = databaseBackupService.backupToZip(file.getParent());
                } else {
                    backupPath = databaseBackupService.exportToSql(file.getParent());
                }
                backupStatusLabel.setText("备份成功: " + new File(backupPath).getName());
                workbench.showInformationDialog("成功", "数据备份成功！\n文件: " + backupPath, buttonType -> {});
            }
        } catch (Exception e) {
            log.error("数据备份失败", e);
            backupStatusLabel.setText("备份失败");
            workbench.showErrorDialog("错误", "数据备份失败: " + e.getMessage(), buttonType -> {});
        }
    }

    @FXML
    private void handleRestore(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择备份文件进行恢复");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SQL文件", "*.sql"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
            );

            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                workbench.showConfirmationDialog("确认", 
                    "恢复数据将覆盖当前所有数据，是否继续？", 
                    buttonType -> {
                        if (buttonType.getButtonData().isDefaultButton()) {
                            try {
                                databaseBackupService.importFromSql(file.getAbsolutePath());
                                backupStatusLabel.setText("恢复成功");
                                workbench.showInformationDialog("成功", "数据恢复成功！请重启软件。", bt -> {});
                            } catch (Exception ex) {
                                log.error("数据恢复失败", ex);
                                backupStatusLabel.setText("恢复失败");
                                workbench.showErrorDialog("错误", "数据恢复失败: " + ex.getMessage(), bt -> {});
                            }
                        }
                    });
            }
        } catch (Exception e) {
            log.error("选择恢复文件失败", e);
            workbench.showErrorDialog("错误", "选择文件失败: " + e.getMessage(), buttonType -> {});
        }
    }
}
