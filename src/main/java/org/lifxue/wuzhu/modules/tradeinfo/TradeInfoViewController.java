/*
 * Copyright 2019 xuelf.
 */
package org.lifxue.wuzhu.modules.tradeinfo;

import com.dlsc.workbenchfx.Workbench;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.constant.CoinConstants;
import org.lifxue.wuzhu.modules.tradeinfo.vo.CoinChoiceBoxVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.viewmodel.TradeInfoViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

@Slf4j
@Component
@FxmlView("TradeInfoView.fxml")
public class TradeInfoViewController implements Initializable {

    @FXML private TableView<TradeInfoVO> dataTable;
    @FXML private TableColumn<TradeInfoVO, Integer> idCol;
    @FXML private TableColumn<TradeInfoVO, Integer> coinIdCol;
    @FXML private TableColumn<TradeInfoVO, String> symbolPairsCol;
    @FXML private TableColumn<TradeInfoVO, String> salebuyCol;
    @FXML private TableColumn<TradeInfoVO, String> priceCol;
    @FXML private TableColumn<TradeInfoVO, String> baseNumCol;
    @FXML private TableColumn<TradeInfoVO, String> quoteNumCol;
    @FXML private TableColumn<TradeInfoVO, String> dateCol;
    @FXML private ChoiceBox<CoinChoiceBoxVO> baseChoiceBox;
    @FXML private ChoiceBox<CoinChoiceBoxVO> quoteChoiceBox;
    @FXML private ChoiceBox<String> salebuyChoiceBox;
    @FXML private DatePicker dateDatePicker;
    @FXML private TextField priceTextField;
    @FXML private TextField numTextField;
    @FXML private TextField totalTextField;

    private Workbench workbench;
    private final TradeInfoViewModel viewModel;

    @Autowired
    public TradeInfoViewController(TradeInfoViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Autowired
    public void setWorkbench(Workbench workbench) {
        this.workbench = workbench;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initTable();
        initChoiceBoxes();
        initBindings();

        viewModel.loadCoins();
        if (!viewModel.getCoinList().isEmpty()) {
            viewModel.loadTradeData(viewModel.getCoinList().get(0).getCoinId());
        }

        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) showErrorDialog(newVal);
        });
    }

    private void initTable() {
        dataTable.setItems(viewModel.getTradeDataList());
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        coinIdCol.setCellValueFactory(new PropertyValueFactory<>("coinId"));
        symbolPairsCol.setCellValueFactory(cellData -> cellData.getValue().symbolPairsProperty());
        salebuyCol.setCellValueFactory(cellData -> cellData.getValue().saleOrBuyProperty());
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        baseNumCol.setCellValueFactory(new PropertyValueFactory<>("baseNum"));
        quoteNumCol.setCellValueFactory(new PropertyValueFactory<>("quoteNum"));
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        dataTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedTrade(newVal);
            showDetails(newVal);
        });
    }

    private void initChoiceBoxes() {
        baseChoiceBox.setConverter(new StringConverter<>() {
            public String toString(CoinChoiceBoxVO o) { return o == null ? null : o.getSymbol(); }
            public CoinChoiceBoxVO fromString(String s) { return null; }
        });
        baseChoiceBox.setItems(viewModel.getCoinList());
        baseChoiceBox.setTooltip(new Tooltip("选择基准货币"));
        baseChoiceBox.getSelectionModel().selectFirst();

        CoinChoiceBoxVO usdt = new CoinChoiceBoxVO(CoinConstants.USDT_SYMBOL, CoinConstants.USDT_COIN_ID);
        quoteChoiceBox.setConverter(new StringConverter<>() {
            public String toString(CoinChoiceBoxVO o) { return o == null ? null : o.getSymbol(); }
            public CoinChoiceBoxVO fromString(String s) { return null; }
        });
        quoteChoiceBox.setItems(FXCollections.observableArrayList(usdt));
        quoteChoiceBox.setTooltip(new Tooltip("选择计价货币"));
        quoteChoiceBox.getSelectionModel().selectFirst();

        salebuyChoiceBox.setItems(FXCollections.observableArrayList("买", "卖"));
        salebuyChoiceBox.setTooltip(new Tooltip("选择交易类型"));

        dateDatePicker.setTooltip(new Tooltip("选择交易时间"));
        dateDatePicker.setValue(LocalDate.now());
    }

    private void initBindings() {
        baseChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedBaseCoin(newVal);
            if (newVal != null) viewModel.loadTradeData(newVal.getCoinId());
        });
        salebuyChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> viewModel.setTradeType(newVal));
        dateDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> viewModel.setTradeDate(newVal));

        priceTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) priceTextField.setText(oldVal);
            else if (!newVal.isEmpty()) {
                viewModel.setPrice(new java.math.BigDecimal(newVal));
                calculateTotal();
            }
        });
        numTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) numTextField.setText(oldVal);
            else if (!newVal.isEmpty()) {
                viewModel.setBaseNum(new java.math.BigDecimal(newVal));
                calculateTotal();
            }
        });
        totalTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) totalTextField.setText(oldVal);
            else if (!newVal.isEmpty()) viewModel.setQuoteNum(new java.math.BigDecimal(newVal));
        });
    }

    private void calculateTotal() {
        viewModel.calculateQuoteNum();
        if (viewModel.getQuoteNum() != null) totalTextField.setText(viewModel.getQuoteNum().toPlainString());
    }

    private void showDetails(TradeInfoVO data) {
        if (data == null) {
            salebuyChoiceBox.setValue("买");
            priceTextField.clear();
            numTextField.clear();
            totalTextField.clear();
            dateDatePicker.setValue(LocalDate.now());
            return;
        }
        int split = data.getSymbolPairs().indexOf('/');
        if (split < 1) { log.error("交易对格式错误"); return; }
        baseChoiceBox.setValue(new CoinChoiceBoxVO(data.getSymbolPairs().substring(0, split), data.getCoinId()));
        salebuyChoiceBox.setValue(data.getSaleOrBuy());
        priceTextField.setText(data.getPrice());
        numTextField.setText(data.getBaseNum());
        totalTextField.setText(data.getQuoteNum());
        dateDatePicker.setValue(LocalDate.parse(data.getDate()));
    }

    @FXML
    private void handleAddData(ActionEvent event) {
        if (viewModel.saveTrade()) {
            priceTextField.clear(); numTextField.clear(); totalTextField.clear();
        } else if (viewModel.getErrorMessage() != null) {
            showErrorDialog(viewModel.getErrorMessage());
        }
    }

    @FXML
    private void handleEdtitData(ActionEvent event) {
        TradeInfoVO selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            workbench.showInformationDialog("提示", "请从表格中选择一行数据!", bt -> {});
            return;
        }
        if (viewModel.updateTrade(selected.getId())) {
            priceTextField.clear(); numTextField.clear(); totalTextField.clear();
        } else {
            showErrorDialog(viewModel.getErrorMessage() != null ? viewModel.getErrorMessage() : "数据库更新错误！");
        }
    }

    @FXML
    private void handleDelData(ActionEvent event) {
        TradeInfoVO selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            workbench.showErrorDialog("错误", "没有选中数据", "请从表格中选择一行数据!", bt -> {});
            return;
        }
        if (viewModel.deleteTrade(selected.getId())) {
            dataTable.getItems().remove(selected);
        } else {
            workbench.showErrorDialog("错误", "数据库删除错误", "选中数据没有从数据库删除!", bt -> {});
        }
    }

    @FXML
    private void handlePriceTextFieldKeyReleased(KeyEvent event) { calculateTotal(); }

    @FXML
    private void handleNumTextFieldKeyReleased(KeyEvent event) { calculateTotal(); }

    private void showErrorDialog(String message) {
        workbench.showErrorDialog("提示", "无效的字段", message, bt -> {});
    }
}
