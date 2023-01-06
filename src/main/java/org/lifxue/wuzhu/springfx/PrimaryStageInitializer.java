package org.lifxue.wuzhu.springfx;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.lifxue.wuzhu.modules.note.NoteModule;
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
    ) {
        this.workbench = workbench;
        this.noteModule = noteModule;
        this.preferencesViewModule = preferencesViewModule;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.stage;
        stage.setTitle("WuZhu");
        stage.getIcons().add(new Image(Objects.requireNonNull(PrimaryStageInitializer.class.getResource("/org/lifxue/wuzhu/images/lifng.jpg")).toString()));

        //增加菜单
        workbench.getToolbarControlsLeft().addAll(
            initFileItem(),
            initUpdateItem()
        );
        //增加模块
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

    /**
     * 初始化文件菜单
     *
     * @return
     */
    private ToolbarItem initFileItem() {
        // 导入CSV菜单项
        MenuItem importItem = new MenuItem("导入CSV", new FontIcon(MaterialDesign.MDI_IMPORT));
        //执行事件监听
        importItem.setOnAction(event -> {
                //ImportTradeData importData = new ImportTradeData(workbench);
                //importData.handleImportData();
            }
        );

        // 导出CSV菜单项
        MenuItem exportItem = new MenuItem("导出CSV", new FontIcon(MaterialDesign.MDI_EXPORT));
        //执行事件监听
        exportItem.setOnAction(event -> {
                //ExportTradeData exportData = new ExportTradeData(workbench);
                //exportData.handleExportData();

            }
        );

        //组合file菜单
        ToolbarItem fileItem = new ToolbarItem("文件", new FontIcon(MaterialDesign.MDI_FILE_WORD));
        fileItem.getItems().addAll(
            importItem,
            exportItem
        );

        return fileItem;
    }

    /**
     * 初始化更新菜单
     *
     * @return
     */
    private ToolbarItem initUpdateItem() {
        // 更新现价菜单项
        MenuItem updatePriceItem = new MenuItem("更新现价", new FontIcon(MaterialDesign.MDI_BANK));
        //执行事件监听
        updatePriceItem.setOnAction(event ->
            workbench.showConfirmationDialog("更新当前价格", "你确定要更新当前价格数据吗？", buttonType -> {
                    if (buttonType == ButtonType.YES) {
                        //CoinInfo coinInfo = new CoinInfo(workbench);
                        //CompletableFuture.runAsync(coinInfo::handleUpdateCurPrice);
                    }
                }
            )
        );

        // 更新MarketCap货币信息菜单项
        MenuItem coinMapItem = new MenuItem("更新货币数据", new FontIcon(MaterialDesign.MDI_DOWNLOAD));
        //执行事件监听
        coinMapItem.setOnAction(event ->
            workbench.showConfirmationDialog("更新货币数据", "你确定要更新货币数据吗？", buttonType -> {
                    if (buttonType == ButtonType.YES) {
                        //CoinInfo coinInfo = new CoinInfo(workbench,null);
                        //CompletableFuture.runAsync(coinInfo::handleUpdateCoinIDMap);
                    }
                }
            )
        );

        // 组合update菜单
        ToolbarItem updateItem = new ToolbarItem("更新", new FontIcon(MaterialDesign.MDI_UPDATE));
        updateItem.getItems().addAll(
            coinMapItem,
            updatePriceItem
        );

        return updateItem;
    }
}
