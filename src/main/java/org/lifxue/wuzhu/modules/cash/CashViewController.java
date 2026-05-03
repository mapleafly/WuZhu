/*
 * Copyright 2019 xuelf.
 */
package org.lifxue.wuzhu.modules.cash;

import com.dlsc.workbenchfx.Workbench;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.modules.tradeinfo.vo.CoinChoiceBoxVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.util.DateHelper;
import org.lifxue.wuzhu.viewmodel.CashViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

@Slf4j
@Component
@FxmlView("CashView.fxml")
public class CashViewController implements Initializable {

    @FXML private TableView<TradeInfoVO> dataTable;
    @FXML private TableColumn<TradeInfoVO, Integer> idCol;
    @FXML private TableColumn<TradeInfoVO, Integer> coinIdCol;
    @FXML private TableColumn<TradeInfoVO, String> baseSymbolCol;
    @FXML private TableColumn<TradeInfoVO, String> salebuyCol;
    @FXML private TableColumn<TradeInfoVO, String> priceCol;
    @FXML private TableColumn<TradeInfoVO, String> baseNumCol;
    @FXML private TableColumn<TradeInfoVO, String> quoteNumCol;
    @FXML private TableColumn<TradeInfoVO, String> dateCol;
    @FXML private ChoiceBox<CoinChoiceBoxVO> baseChoiceBox;
    @FXML private ChoiceBox<String> salebuyChoiceBox;
    @FXML private DatePicker dateDatePicker;
    @FXML private TextField numTextField;

    private Workbench workbench;
    private final CashViewModel viewModel;

    @Autowired
    public CashViewController(CashViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Autowired
    public void setWorkbench(Workbench workbench) {
        this.workbench = workbench;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initTable();
        initControls();
        initBindings();

        viewModel.initializeCoins();
        if (!viewModel.getCoinList().isEmpty()) {
            viewModel.loadCashData(viewModel.getCoinList().get(0).getCoinId());
        }

        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) showErrorDialog(newVal);
        });
    }

    private void initTable() {
        dataTable.setItems(viewModel.getCashDataList());
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        coinIdCol.setCellValueFactory(new PropertyValueFactory<>("coinId"));
        baseSymbolCol.setCellValueFactory(cellData -> cellData.getValue().symbolPairsProperty());
        salebuyCol.setCellValueFactory(cellData -> cellData.getValue().saleOrBuyProperty());
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        baseNumCol.setCellValueFactory(new PropertyValueFactory<>("baseNum"));
        quoteNumCol.setCellValueFactory(new PropertyValueFactory<>("quoteNum"));
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        dataTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedRecord(newVal);
            showDetails(newVal);
        });
    }

    private void initControls() {
        baseChoiceBox.setConverter(new StringConverter<>() {
            public String toString(CoinChoiceBoxVO o) { return o == null ? null : o.getSymbol(); }
            public CoinChoiceBoxVO fromString(String s) { return null; }
        });
        baseChoiceBox.setItems(viewModel.getCoinList());
        baseChoiceBox.setTooltip(new Tooltip("选择货币"));
        baseChoiceBox.getSelectionModel().selectFirst();

        salebuyChoiceBox.setItems(viewModel.getTransactionTypes());
        salebuyChoiceBox.setTooltip(new Tooltip("选择出入金类型"));

        dateDatePicker.setConverter(DateHelper.CONVERTER);
        dateDatePicker.setTooltip(new Tooltip("选择操作时间"));
        dateDatePicker.setValue(LocalDate.now());
    }

    private void initBindings() {
        baseChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedCoin(newVal);
            if (newVal != null) viewModel.loadCashData(newVal.getCoinId());
        });
        salebuyChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> viewModel.setTransactionType(newVal));
        dateDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> viewModel.setTransactionDate(newVal));

        numTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) numTextField.setText(oldVal);
            else if (!newVal.isEmpty()) viewModel.setAmount(new java.math.BigDecimal(newVal));
        });
    }

    private void showDetails(TradeInfoVO record) {
        if (record == null) {
            salebuyChoiceBox.setValue("入金");
            numTextField.clear();
            dateDatePicker.setValue(LocalDate.now());
        } else {
            salebuyChoiceBox.setValue("卖".equals(record.getSaleOrBuy()) ? "入金" : "出金");
            numTextField.setText(record.getBaseNum());
            dateDatePicker.setValue(LocalDate.parse(record.getDate()));
        }
    }

    @FXML
    private void handleAddData(ActionEvent event) {
        if (viewModel.saveCashRecord()) numTextField.clear();
        else if (viewModel.getErrorMessage() != null) showErrorDialog(viewModel.getErrorMessage());
    }

    @FXML
    private void handleEdtitData(ActionEvent event) {
        TradeInfoVO selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            workbench.showErrorDialog("提示", "没有选中数据", "请从表格中选择一行数据!", bt -> {});
            return;
        }
        if (viewModel.updateCashRecord(selected.getId())) numTextField.clear();
        else showErrorDialog(viewModel.getErrorMessage() != null ? viewModel.getErrorMessage() : "数据库更新错误！");
    }

    @FXML
    private void handleDelData(ActionEvent event) {
        TradeInfoVO selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            workbench.showErrorDialog("错误", "没有选中数据", "请从表格中选择一行数据!", bt -> {});
            return;
        }
        if (viewModel.deleteCashRecord(selected.getId())) dataTable.getItems().remove(selected);
        else workbench.showErrorDialog("错误", "数据库删除错误", "选中数据没有从数据库删除!", bt -> {});
    }

    private void showErrorDialog(String message) {
        workbench.showErrorDialog("提示", "无效的字段", message, bt -> {});
    }
}
