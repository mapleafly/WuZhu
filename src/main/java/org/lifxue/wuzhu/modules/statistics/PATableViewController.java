/*
 * Copyright 2019 xuelf.
 */
package org.lifxue.wuzhu.modules.statistics;

import com.dlsc.workbenchfx.Workbench;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.modules.statistics.vo.PATableVO;
import org.lifxue.wuzhu.util.DateHelper;
import org.lifxue.wuzhu.viewmodel.PATableViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

@Component
@FxmlView("PATableView.fxml")
public class PATableViewController implements Initializable {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ChoiceBox<String> typeChoiceBox;
    @FXML private ChoiceBox<String> tradeChoiceBox;
    @FXML private Label curCHGLabel;
    @FXML private Label paLabel;
    @FXML private Label numTotalLabel;
    @FXML private Label nowPriceTotalLabel;
    @FXML private Label PriceTotalLabel;
    @FXML private Label nowpaLabel;
    @FXML private TableView<PATableVO> tradeDataTable;
    @FXML private TableColumn<PATableVO, Integer> idCol;
    @FXML private TableColumn<PATableVO, Integer> coinIdCol;
    @FXML private TableColumn<PATableVO, String> symbolPairsCol;
    @FXML private TableColumn<PATableVO, String> chgCol;
    @FXML private TableColumn<PATableVO, String> buyOrSaleCol;
    @FXML private TableColumn<PATableVO, String> priceCol;
    @FXML private TableColumn<PATableVO, String> baseNumCol;
    @FXML private TableColumn<PATableVO, String> quoteNumCol;
    @FXML private TableColumn<PATableVO, String> dateCol;

    private Workbench workbench;
    private final PATableViewModel viewModel;

    @Autowired
    public PATableViewController(PATableViewModel viewModel) {
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
        viewModel.loadAvailableSymbols();
        viewModel.errorMessageProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) workbench.showErrorDialog("警告", "无效的字段！", newVal, bt -> {});
        });
    }

    private void initTable() {
        tradeDataTable.setItems(viewModel.getPaDataList());
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        coinIdCol.setCellValueFactory(new PropertyValueFactory<>("coinId"));
        symbolPairsCol.setCellValueFactory(cellData -> cellData.getValue().symbolPairsProperty());
        chgCol.setCellValueFactory(cellData -> cellData.getValue().chgProperty());
        buyOrSaleCol.setCellValueFactory(cellData -> cellData.getValue().saleOrBuyProperty());
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        baseNumCol.setCellValueFactory(new PropertyValueFactory<>("baseNum"));
        quoteNumCol.setCellValueFactory(new PropertyValueFactory<>("quoteNum"));
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
    }

    private void initControls() {
        tradeChoiceBox.setItems(viewModel.getTradeTypes());
        tradeChoiceBox.setValue("全部");
        tradeChoiceBox.setTooltip(new Tooltip("选择交易类型"));

        typeChoiceBox.setItems(viewModel.getAvailableSymbols());
        typeChoiceBox.setValue("全部品种");
        typeChoiceBox.setTooltip(new Tooltip("选择交易品种，默认为全部品种"));

        startDatePicker.setConverter(DateHelper.CONVERTER);
        startDatePicker.setTooltip(new Tooltip("选择初始时间"));
        startDatePicker.setEditable(false);
        startDatePicker.setValue(LocalDate.of(2009, 1, 3));

        endDatePicker.setConverter(DateHelper.CONVERTER);
        endDatePicker.setTooltip(new Tooltip("选择结束时间"));
        endDatePicker.setEditable(false);
        endDatePicker.setValue(LocalDate.now());
    }

    private void initBindings() {
        curCHGLabel.textProperty().bind(viewModel.curCHGProperty());
        nowPriceTotalLabel.textProperty().bind(viewModel.nowPriceTotalProperty());
        paLabel.textProperty().bind(viewModel.paPriceProperty());
        numTotalLabel.textProperty().bind(viewModel.numTotalProperty());
        nowpaLabel.textProperty().bind(viewModel.nowPriceProperty());
        PriceTotalLabel.textProperty().bind(viewModel.paPriceTotalProperty());

        viewModel.startDateProperty().bind(startDatePicker.valueProperty());
        viewModel.endDateProperty().bind(endDatePicker.valueProperty());
        viewModel.selectedSymbolProperty().bind(typeChoiceBox.getSelectionModel().selectedItemProperty());
        viewModel.selectedTradeTypeProperty().bind(tradeChoiceBox.getSelectionModel().selectedItemProperty());
    }

    @FXML
    private void handleSearchOnAction(ActionEvent event) {
        viewModel.loadPAData();
    }
}
