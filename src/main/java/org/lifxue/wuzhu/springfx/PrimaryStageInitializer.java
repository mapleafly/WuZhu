package org.lifxue.wuzhu.springfx;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.lifxue.wuzhu.modules.note.NoteModule;
import org.lifxue.wuzhu.modules.setting.PreferencesViewModule;
import org.lifxue.wuzhu.themes.InterfaceTheme;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @version 1.0
 * @classname PrimaryStageInitializer
 * @description 监听事件，对事件的处理即启动javafx
 * @auhthor lifxue
 * @date 2023/1/6 14:19
 */
@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final Workbench workbench;

    private final NoteModule noteModule;
    private final PreferencesViewModule preferencesViewModule;
    private final InterfaceTheme interfaceTheme;

    public PrimaryStageInitializer(
        Workbench workbench,
        NoteModule noteModule,
        PreferencesViewModule preferencesViewModule,
        InterfaceTheme interfaceTheme
    ) {
        this.workbench = workbench;
        this.noteModule = noteModule;
        this.preferencesViewModule = preferencesViewModule;
        this.interfaceTheme = interfaceTheme;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.stage;
        stage.setTitle("WuZhu");
        stage.getIcons().add(new Image(Objects.requireNonNull(
            PrimaryStageInitializer.class.getResource("/org/lifxue/wuzhu/images/W-p5.png")).toString()));

        //增加侧滑导航栏
        workbench.getNavigationDrawer().getItems().addAll(
            initFileMenu(),
            initUpdateMenu(),
            preferencesMenuItem()
        );

        //增加菜单
        //workbench.getToolbarControlsLeft().addAll(
        //    initFileToolbarItem(),
        //    initUpdateToolbarItem()
        //);

        //增加模块
        workbench.getModules().addAll(
            noteModule,
            preferencesViewModule
        );

        workbench.setModulesPerPage(9);

        Scene scene = new Scene(workbench);
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.show();
        stage.centerOnScreen();

        // 设置主题
        interfaceTheme.initNightMode();
    }

    private MenuItem preferencesMenuItem() {
        MenuItem preferencesMenuItem = new MenuItem("首选项", new FontIcon(MaterialDesign.MDI_SETTINGS));
        preferencesMenuItem.setOnAction(event -> {
            workbench.openModule(preferencesViewModule);
            workbench.hideNavigationDrawer();
        });

        return preferencesMenuItem;
    }

    private MenuItem csvImportItem() {
        // 导入CSV菜单项
        MenuItem importItem = new MenuItem("导入CSV", new FontIcon(MaterialDesign.MDI_IMPORT));
        //执行事件监听
        importItem.setOnAction(event -> {
                //ImportTradeData importData = new ImportTradeData(workbench);
                //importData.handleImportData();
                workbench.hideNavigationDrawer();
            }
        );

        return importItem;
    }

    private MenuItem csvExportItem() {
        // 导出CSV菜单项
        MenuItem exportItem = new MenuItem("导出CSV", new FontIcon(MaterialDesign.MDI_EXPORT));
        //执行事件监听
        exportItem.setOnAction(event -> {
                //ExportTradeData exportData = new ExportTradeData(workbench);
                //exportData.handleExportData();
                workbench.hideNavigationDrawer();
            }
        );

        return exportItem;
    }

    private Menu initFileMenu() {
        Menu fileMenu = new Menu("文件", new FontIcon(MaterialDesign.MDI_FILE_WORD));
        fileMenu.getItems().addAll(
            csvImportItem(),
            csvExportItem()
        );
        return fileMenu;
    }

    /**
     * @return com.dlsc.workbenchfx.view.controls.ToolbarItem
     * @description 初始化文件菜单
     * @author lifxue
     * @date 2023/1/6 14:21
     **/
    private ToolbarItem initFileToolbarItem() {
        //组合file菜单
        ToolbarItem fileItem = new ToolbarItem("文件", new FontIcon(MaterialDesign.MDI_FILE_WORD));
        fileItem.getItems().addAll(
            csvImportItem(),
            csvExportItem()
        );

        return fileItem;
    }

    private MenuItem updatePriceItem() {
        // 更新现价菜单项
        MenuItem updatePriceItem = new MenuItem("更新现价", new FontIcon(MaterialDesign.MDI_BANK));
        //执行事件监听
        updatePriceItem.setOnAction(event ->
            workbench.showConfirmationDialog("更新当前价格", "你确定要更新当前价格数据吗？", buttonType -> {
                    if (buttonType == ButtonType.YES) {
                        //CoinInfo coinInfo = new CoinInfo(workbench);
                        //CompletableFuture.runAsync(coinInfo::handleUpdateCurPrice);
                        workbench.hideNavigationDrawer();
                    }
                }
            )
        );

        return updatePriceItem;
    }

    private MenuItem coinMapItem() {
        // 更新MarketCap货币信息菜单项
        MenuItem coinMapItem = new MenuItem("更新货币数据", new FontIcon(MaterialDesign.MDI_DOWNLOAD));
        //执行事件监听
        coinMapItem.setOnAction(event ->
            workbench.showConfirmationDialog("更新货币数据", "你确定要更新货币数据吗？", buttonType -> {
                    if (buttonType == ButtonType.YES) {
                        //CoinInfo coinInfo = new CoinInfo(workbench,null);
                        //CompletableFuture.runAsync(coinInfo::handleUpdateCoinIDMap);
                        workbench.hideNavigationDrawer();
                    }
                }
            )
        );

        return coinMapItem;
    }

    private Menu initUpdateMenu() {
        Menu updateMenu = new Menu("更新", new FontIcon(MaterialDesign.MDI_UPDATE));
        updateMenu.getItems().addAll(
            coinMapItem(),
            updatePriceItem()
        );
        return updateMenu;
    }

    /**
     * @return com.dlsc.workbenchfx.view.controls.ToolbarItem
     * @description 初始化更新菜单
     * @author lifxue
     * @date 2023/1/6 14:21
     **/
    private ToolbarItem initUpdateToolbarItem() {
        // 组合update菜单
        ToolbarItem updateItem = new ToolbarItem("更新", new FontIcon(MaterialDesign.MDI_UPDATE));
        updateItem.getItems().addAll(
            coinMapItem(),
            updatePriceItem()
        );

        return updateItem;
    }
}
