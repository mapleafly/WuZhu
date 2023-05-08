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
package org.lifxue.wuzhu.modules.cash;

import com.dlsc.workbenchfx.Workbench;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.TradeInfoJpa;
import org.lifxue.wuzhu.service.ITradeInfoJpaService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.lifxue.wuzhu.util.DateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author lif
 */
@Component
@FxmlView("CashView.fxml")
public class CashViewController implements Initializable {

    // USDT
    private static final String BASESYMBOL = "USDT";
    private static final String BASEID = "825";
    /**
     * The data as an observable list of TradeData.
     */
    private final ObservableList<TradeInfoVO> tradeDataList;

    private List<String> coinList;
    @FXML
    private TableView<TradeInfoVO> dataTable;
    @FXML
    private TableColumn<TradeInfoVO, Integer> idCol;
    @FXML
    private TableColumn<TradeInfoVO, Integer> coinIdCol;
    @FXML
    private TableColumn<TradeInfoVO, String> baseSymbolCol;
    @FXML
    private TableColumn<TradeInfoVO, String> salebuyCol;
    @FXML
    private TableColumn<TradeInfoVO, String> priceCol;
    @FXML
    private TableColumn<TradeInfoVO, String> baseNumCol;
    @FXML
    private TableColumn<TradeInfoVO, String> quoteNumCol;
    @FXML
    private TableColumn<TradeInfoVO, String> dateCol;
    @FXML
    private ChoiceBox<String> baseChoiceBox;
    @FXML
    private ChoiceBox<String> salebuyChoiceBox;
    @FXML
    private DatePicker dateDatePicker;
    @FXML
    private TextField numTextField;

    private Workbench workbench;

    private final ITradeInfoJpaService iTradeInfoJpaService;

    /***
     * @description
     * @author lifxue
     * @date 2023/2/12 17:09
     * @param workbench
     * @return void
     **/
    @Autowired
    public void setWorkbench(Workbench workbench) {
        this.workbench = workbench;
    }

    @Autowired
    public CashViewController(ITradeInfoJpaService iTradeInfoJpaService) {
        this.iTradeInfoJpaService = iTradeInfoJpaService;
        tradeDataList = FXCollections.observableArrayList();
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        tradeDataList.clear();

        //获取数据
        coinList = new ArrayList<>();
        coinList.add(BASESYMBOL);
        if (coinList != null && !coinList.isEmpty()) {
            List<TradeInfoVO> tradeInfoList = iTradeInfoJpaService.queryTradeInfoByBaseSymbol(coinList.get(0));
            if (tradeInfoList != null && !tradeInfoList.isEmpty()) {
                this.tradeDataList.addAll(tradeInfoList);
            }
        }


        dataTable.setItems(tradeDataList);

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        coinIdCol.setCellValueFactory(new PropertyValueFactory<>("coinId"));
        baseSymbolCol.setCellValueFactory(cellData -> cellData.getValue().symbolPairsProperty());
        salebuyCol.setCellValueFactory(cellData -> cellData.getValue().saleOrBuyProperty());
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        baseNumCol.setCellValueFactory(new PropertyValueFactory<>("baseNum"));
        quoteNumCol.setCellValueFactory(new PropertyValueFactory<>("quoteNum"));
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        baseChoiceBox.setItems(FXCollections.observableArrayList(coinList));
        baseChoiceBox.setTooltip(new Tooltip("选择货币"));
        baseChoiceBox.getSelectionModel().selectFirst();

        salebuyChoiceBox.setItems(FXCollections.observableArrayList("入金", "出金"));
        salebuyChoiceBox.setTooltip(new Tooltip("选择出入金类型"));

        dateDatePicker.setConverter(DateHelper.CONVERTER);
        // dateDatePicker.setPromptText(pattern.toLowerCase());
        dateDatePicker.setTooltip(new Tooltip("选择操作时间"));

        showTradeDataDetails(null);

        dataTable
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> showTradeDataDetails(newValue));

        baseChoiceBox
            .getSelectionModel()
            .selectedIndexProperty()
            .addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (newValue.intValue() >= 0) {
                        this.tradeDataList.clear();
                        String selectedCoin = this.coinList.get(newValue.intValue());
                        List<TradeInfoVO> tradeInfoVOS = iTradeInfoJpaService.queryTradeInfoByBaseSymbol(selectedCoin);
                        if (tradeInfoVOS != null && !tradeInfoVOS.isEmpty()) {
                            this.tradeDataList.addAll(tradeInfoVOS);
                        }
                    }
                });

        numTextField
            .textProperty()
            .addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (!newValue.matches("\\d*(\\.\\d*)?")) {
                        numTextField.setText(oldValue);
                    }
                });
    }

    @FXML
    private void handleAddData(ActionEvent event) {
        if (isInputValid()) {
            TradeInfoJpa tradeInfo = new TradeInfoJpa();
            setTradeInfo(tradeInfo);
            if (iTradeInfoJpaService.save(tradeInfo)) {
                tradeDataList.add(0, CopyUtil.copyjpa(tradeInfo));
                numTextField.setText("");
            }
           /* TradeDataBean bean = new TradeDataBean();
            setTradeDataBean(bean);
            Integer id = TradeDataDao.insert(bean);
            if (id != -1) {
                bean.setId(id);
                tradeDataList.add(0, beanToFXC(bean));
                numTextField.setText("");
            }*/
        }
    }

    private void setTradeInfo(TradeInfoJpa tradeInfo) {
        tradeInfo.setBaseId(iTradeInfoJpaService.queryCoinBySymbol(baseChoiceBox.getValue()).getId());
        tradeInfo.setBaseSymbol(baseChoiceBox.getValue());
        tradeInfo.setQuoteId(Integer.valueOf(BASEID));
        tradeInfo.setQuoteSymbol(BASESYMBOL);
        if (salebuyChoiceBox.getValue().equals("入金")) {
            tradeInfo.setSaleOrBuy("卖");
        } else {
            tradeInfo.setSaleOrBuy("买");
        }
        tradeInfo.setPrice("1");
        tradeInfo.setBaseNum(numTextField.getText());
        tradeInfo.setQuoteNum(numTextField.getText());
        tradeInfo.setTradeDate(DateHelper.toString(this.dateDatePicker.getValue()));

    }
    /*private void setTradeDataBean(TradeDataBean bean) {
        bean.setBase_id(TradeDataDao.queryCoinBySymbol(baseChoiceBox.getValue()).getId());
        bean.setBase_symbol(baseChoiceBox.getValue());
        bean.setQuote_id(Integer.valueOf(BASEID));
        bean.setQuote_symbol(BASESYMBOL);
        if (salebuyChoiceBox.getValue().equals("入金")) {
            bean.setSale_or_buy("卖");
        } else {
            bean.setSale_or_buy("买");
        }
        bean.setPrice("1");
        bean.setBase_num(numTextField.getText());
        bean.setQuote_num(numTextField.getText());
        bean.setTrade_date(DateHelper.toString(this.dateDatePicker.getValue()));
    }*/

    @FXML
    private void handleEdtitData(ActionEvent event) {
        if (isInputValid()) {
            int selectedIndex = dataTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                TradeInfoVO tradeInfoVO = dataTable.getItems().get(selectedIndex);
                TradeInfoJpa tradeInfo = iTradeInfoJpaService.findById(tradeInfoVO.getId());
                setTradeInfo(tradeInfo);

                if (iTradeInfoJpaService.save(tradeInfo)) {
                    tradeInfoVO = CopyUtil.copyjpa(tradeInfo);
                    for (int i = 0; i < tradeDataList.size(); i++) {
                        if (tradeDataList.get(i).getId().equals(tradeInfoVO.getId())) {
                            tradeDataList.remove(i);
                            tradeDataList.add(i, tradeInfoVO);
                            dataTable.getSelectionModel().select(i);
                            break;
                        }
                    }
                } else {
                    workbench.showErrorDialog("错误", "数据库更新错误！", "选中的数据没有被数据库更新!", buttonType -> {
                    });
                }
            } else {
                // Nothing selected.
                workbench.showErrorDialog("提示", "没有选中数据", "请从表格中选择一行数据!", buttonType -> {
                });
            }
        }
    }

    @FXML
    private void handleDelData(ActionEvent event) {
        int selectedIndex = dataTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            TradeInfoVO tradeInfoVO = dataTable.getItems().get(selectedIndex);
            if (iTradeInfoJpaService.deleteById(tradeInfoVO.getId())) {
                // 整理表格
                dataTable.getItems().remove(selectedIndex);
            } else {
                workbench.showErrorDialog("错误", "数据库删除错误", "选中数据没有从数据库删除!", buttonType -> {
                });
            }
        } else {
            // Nothing selected.
            workbench.showErrorDialog("错误", "没有选中数据", "请从表格中选择一行数据!", buttonType -> {
            });
        }
    }


    /**
     * @param tradeData 1
     * @Description:
     * @return: void
     * @author: mapleaf
     * @date: 2020/6/23 16:57
     */
    private void showTradeDataDetails(TradeInfoVO tradeData) {
        if (tradeData != null) {
            String symbolPairs = tradeData.getSymbolPairs();
            baseChoiceBox.setValue(symbolPairs);
            salebuyChoiceBox.setValue(tradeData.getSaleOrBuy());
            numTextField.setText(tradeData.getBaseNum());
            dateDatePicker.setValue(DateHelper.fromString(tradeData.getDate()));
        } else {
            salebuyChoiceBox.setValue("入金");
            numTextField.setText("");
            dateDatePicker.setValue(LocalDate.now());
        }
    }

    /**
     * Validates the user inpu.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (baseChoiceBox.getValue() == null || baseChoiceBox.getValue().length() == 0) {
            errorMessage += "无效的类别!\n";
        }
        if (salebuyChoiceBox.getValue() == null || salebuyChoiceBox.getValue().length() == 0) {
            errorMessage += "无效的买/卖!\n";
        }
        if (!DateHelper.validDate(DateHelper.toString(dateDatePicker.getValue()))
            || dateDatePicker.getValue() == null) {
            errorMessage += "无效的时间!\n";
        }

        if (numTextField.getText() == null || numTextField.getText().length() == 0) {
            errorMessage += "无效的数量!\n";
        } else {
            // try to parse the num into an double.
            try {
                Double.parseDouble(numTextField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "无效的数量(必须是整数或小数)!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            workbench.showErrorDialog("提示", "无效的字段", errorMessage, buttonType -> {
            });

            return false;
        }
    }

}
