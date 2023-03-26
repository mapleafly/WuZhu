package org.lifxue.wuzhu.springfx;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.lifxue.wuzhu.modules.cash.CashViewModule;
import org.lifxue.wuzhu.modules.file.ExportTradeData;
import org.lifxue.wuzhu.modules.file.ImportTradeData;
import org.lifxue.wuzhu.modules.note.NoteModule;
import org.lifxue.wuzhu.modules.piechart.TypePieChartViewModule;
import org.lifxue.wuzhu.modules.selectcoin.SelectCoinViewModule;
import org.lifxue.wuzhu.modules.setting.PreferencesViewModule;
import org.lifxue.wuzhu.modules.statistics.PATableViewModule;
import org.lifxue.wuzhu.modules.tradeinfo.TradeInfoViewModule;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.ICMCQuotesLatestService;
import org.lifxue.wuzhu.themes.InterfaceTheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @version 1.0
 * @classname PrimaryStageInitializer
 * @description 监听事件，对事件的处理即启动javafx
 * @auhthor lifxue
 * @date 2023/1/6 14:19
 */
@Slf4j
@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    @Value("${spring.application.name}")
    private String applicationName;
    private final Workbench workbench;

    private final NoteModule noteModule;
    private final PreferencesViewModule preferencesViewModule;
    private final SelectCoinViewModule selectCoinViewModule;

    private final TradeInfoViewModule tradeInfoViewModule;

    private final InterfaceTheme interfaceTheme;

    private final ICMCMapService icmcMapService;

    private final ICMCQuotesLatestService icmcQuotesLatestService;

    private final ImportTradeData importTradeData;

    private final ExportTradeData exportTradeData;

    private final PATableViewModule paTableViewModule;
    private final TypePieChartViewModule typePieChartViewModule;

    private final CashViewModule cashViewModule;

    public PrimaryStageInitializer(
        Workbench workbench,
        NoteModule noteModule,
        PreferencesViewModule preferencesViewModule,
        InterfaceTheme interfaceTheme,
        ICMCMapService icmcMapService,
        ICMCQuotesLatestService icmcQuotesLatestService,
        SelectCoinViewModule selectCoinViewModule,
        TradeInfoViewModule tradeInfoViewModule,
        ImportTradeData importTradeData,
        ExportTradeData exportTradeData,
        PATableViewModule paTableViewModule,
        TypePieChartViewModule typePieChartViewModule,
        CashViewModule cashViewModule
    ) {
        this.workbench = workbench;
        this.interfaceTheme = interfaceTheme;
        this.icmcMapService = icmcMapService;
        this.icmcQuotesLatestService = icmcQuotesLatestService;

        this.noteModule = noteModule;
        this.preferencesViewModule = preferencesViewModule;
        this.selectCoinViewModule = selectCoinViewModule;
        this.tradeInfoViewModule = tradeInfoViewModule;
        this.importTradeData = importTradeData;
        this.exportTradeData = exportTradeData;
        this.paTableViewModule = paTableViewModule;
        this.typePieChartViewModule = typePieChartViewModule;
        this.cashViewModule = cashViewModule;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.stage;
        stage.setTitle(applicationName);
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
            tradeInfoViewModule,
            cashViewModule,
            paTableViewModule,
            typePieChartViewModule,
            selectCoinViewModule,
            preferencesViewModule
        );

        //设置每页显示几个模块
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
                importTradeData.handleImportData();
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
                exportTradeData.handleExportData();
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
                        CompletableFuture.runAsync(() -> {
                            if (icmcQuotesLatestService.saveBatch()) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        workbench.showInformationDialog("价格更新信息", "价格信息更新成功！",
                                            buttonType1 -> {
                                            }
                                        );
                                    }
                                });
                            } else {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        workbench.showErrorDialog("价格更新失败", "请检查网络是否通畅，是否有关注的币种被选择！",
                                            buttonType1 -> {
                                            }
                                        );
                                    }
                                });
                            }
                        });
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
                        CompletableFuture.runAsync(() -> {
                            if (icmcMapService.saveNewBatch("cmc_rank")) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        workbench.showInformationDialog("数据更新信息", "加密货币信息更新成功！",
                                            buttonType1 -> {
                                            }
                                        );
                                    }
                                });
                            } else {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        workbench.showErrorDialog("数据更新信息", "加密货币信息更新失败！",
                                            buttonType1 -> {
                                            }
                                        );
                                    }
                                });
                            }
                        });
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
