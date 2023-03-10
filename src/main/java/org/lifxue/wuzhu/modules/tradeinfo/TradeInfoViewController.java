/*
 * Copyright 2019 xuelf.
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
package org.lifxue.wuzhu.modules.tradeinfo;

import com.dlsc.workbenchfx.Workbench;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.entity.TradeInfo;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.service.ITradeInfoService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.lifxue.wuzhu.util.DateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author xuelf
 */

@Slf4j
@Component
@FxmlView("TradeInfoView.fxml")
public class TradeInfoViewController implements Initializable {
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
    private TableColumn<TradeInfoVO, String> symbolPairsCol;
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
    private ChoiceBox<String> quoteChoiceBox;
    @FXML
    private ChoiceBox<String> salebuyChoiceBox;
    @FXML
    private DatePicker dateDatePicker;
    @FXML
    private TextField priceTextField;
    @FXML
    private TextField numTextField;
    @FXML
    private TextField totalTextField;

    private Workbench workbench;
    private final ITradeInfoService iTradeInfoService;

    @Autowired
    public TradeInfoViewController(ITradeInfoService iTradeInfoService) {

        this.iTradeInfoService = iTradeInfoService;
        tradeDataList = FXCollections.observableArrayList();


    }

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

    /**
     * @param url 1
     * @param rb  2
     * @Description: Initializes the controller class.
     * @return: void
     * @author: mapleaf
     * @date: 2020/6/23 16:52
     */
    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        tradeDataList.clear();
        //????????????
        coinList = iTradeInfoService.queryCurSymbol();
        if (coinList != null && !coinList.isEmpty()) {
            List<TradeInfoVO> tradeInfoList = iTradeInfoService.queryTradeInfo(coinList.get(0));
            if(tradeInfoList != null && !tradeInfoList.isEmpty()) {
                this.tradeDataList.addAll(tradeInfoList);
            }
        }

        dataTable.setItems(tradeDataList);

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        coinIdCol.setCellValueFactory(new PropertyValueFactory<>("coinId"));
        symbolPairsCol.setCellValueFactory(cellData -> cellData.getValue().symbolPairsProperty());
        salebuyCol.setCellValueFactory(cellData -> cellData.getValue().saleOrBuyProperty());
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        baseNumCol.setCellValueFactory(new PropertyValueFactory<>("baseNum"));
        quoteNumCol.setCellValueFactory(new PropertyValueFactory<>("quoteNum"));
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        baseChoiceBox.setItems(FXCollections.observableArrayList(coinList));
        baseChoiceBox.setTooltip(new Tooltip("??????????????????"));
        baseChoiceBox.getSelectionModel().selectFirst();

        quoteChoiceBox.setItems(FXCollections.observableArrayList("USDT"));
        quoteChoiceBox.setTooltip(new Tooltip("??????????????????"));
        quoteChoiceBox.getSelectionModel().selectFirst();

        salebuyChoiceBox.setItems(FXCollections.observableArrayList("???", "???"));
        salebuyChoiceBox.setTooltip(new Tooltip("??????????????????"));

        dateDatePicker.setConverter(DateHelper.CONVERTER);
        // dateDatePicker.setPromptText(pattern.toLowerCase());
        dateDatePicker.setTooltip(new Tooltip("??????????????????"));

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
                        String selectedCoinSymbol = this.coinList.get(newValue.intValue());
                        List<TradeInfoVO> tradeInfoVOS = iTradeInfoService.queryTradeInfo(selectedCoinSymbol);
                        if(tradeInfoVOS!= null &&!tradeInfoVOS.isEmpty()) {
                            this.tradeDataList.addAll(tradeInfoVOS);
                        }
                    }
                });

        priceTextField
            .textProperty()
            .addListener(
                (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (!newValue.matches("\\d*(\\.\\d*)?")) {
                        priceTextField.setText(oldValue);
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
        totalTextField
            .textProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*(\\.\\d*)?")) {
                        totalTextField.setText(oldValue);
                    }
                });
    }

    @FXML
    private void handleAddData(ActionEvent event) {
        if (isInputValid()) {
            TradeInfo tradeInfo = new TradeInfo();
            setTradeInfo(tradeInfo);
            if(iTradeInfoService.save(tradeInfo)){
                tradeDataList.add(0, CopyUtil.copy(tradeInfo));
                priceTextField.setText("");
                numTextField.setText("");
                totalTextField.setText("");
            }
        }
    }

    private void setTradeInfo(TradeInfo tradeInfo) {
        tradeInfo.setBaseId(iTradeInfoService.queryCoinBySymbol(baseChoiceBox.getValue()).getId());
        tradeInfo.setBaseSymbol(baseChoiceBox.getValue());
        tradeInfo.setQuoteId(iTradeInfoService.queryCoinBySymbol(quoteChoiceBox.getValue()).getId());
        tradeInfo.setQuoteSymbol(quoteChoiceBox.getValue());
        tradeInfo.setSaleOrBuy(salebuyChoiceBox.getValue());
        tradeInfo.setPrice(priceTextField.getText());
        tradeInfo.setBaseNum(numTextField.getText());
        tradeInfo.setQuoteNum(totalTextField.getText());
        tradeInfo.setTradeDate(DateHelper.toString(this.dateDatePicker.getValue()));

    }

    @FXML
    private void handleEdtitData(ActionEvent event) {
        if (isInputValid()) {
            int selectedIndex = dataTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                TradeInfoVO tradeInfoVO = dataTable.getItems().get(selectedIndex);
                TradeInfo tradeInfo = iTradeInfoService.getById(tradeInfoVO.getId());
                setTradeInfo(tradeInfo);
                if (iTradeInfoService.updateById(tradeInfo)) {
                    tradeInfoVO = CopyUtil.copy(tradeInfo);
                    for (int i = 0; i < tradeDataList.size(); i++) {
                        if (tradeDataList.get(i).getId().equals(tradeInfoVO.getId())) {
                            tradeDataList.remove(i);
                            tradeDataList.add(i, tradeInfoVO);
                            dataTable.getSelectionModel().select(i);
                            break;
                        }
                    }
                } else {
                    workbench.showErrorDialog("??????", "????????????????????????", "???????????????????????????????????????!", buttonType -> {
                    });
                }
            } else {
                // Nothing selected.
                workbench.showInformationDialog("??????", "??????????????????", buttonType -> {
                });
            }
        }
    }

    @FXML
    private void handleDelData(ActionEvent event) {
        int selectedIndex = dataTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            TradeInfoVO tradeInfoVO = dataTable.getItems().get(selectedIndex);
            if (iTradeInfoService.removeById(tradeInfoVO.getId())) {
                // ????????????
                dataTable.getItems().remove(selectedIndex);
            } else {
                workbench.showErrorDialog("??????", "?????????????????????", "????????????????????????????????????!", buttonType -> {
                });
            }
        } else {
            // Nothing selected.
            workbench.showErrorDialog("??????", "??????????????????", "?????????????????????????????????!", buttonType -> {
            });
        }
    }

    @FXML
    private void handlePriceTextFieldKeyReleased(KeyEvent event) {
        if (numTextField.getText().trim().equals("") || priceTextField.getText().trim().equals("")) {
            return;
        }
        BigDecimal num = new BigDecimal(numTextField.getText());
        BigDecimal price = new BigDecimal(priceTextField.getText());
        totalTextField.setText(num.multiply(price).setScale(12, RoundingMode.HALF_UP).toPlainString());
    }

    @FXML
    private void handleNumTextFieldKeyReleased(KeyEvent event) {
        if (numTextField.getText().trim().equals("") || priceTextField.getText().trim().equals("")) {
            return;
        }
        BigDecimal num = new BigDecimal(numTextField.getText());
        BigDecimal price = new BigDecimal(priceTextField.getText());
        totalTextField.setText(num.multiply(price).setScale(12, RoundingMode.HALF_UP).toPlainString());
    }

    /**
     * @param tradeData 1
     * @Description:
     * @return: void
     * @author: mapleaf
     * @date: 2020/6/23 16:54
     */
    private void showTradeDataDetails(TradeInfoVO tradeData) {
        if (tradeData != null) {
            String symbolPairs = tradeData.getSymbolPairs();
            int split = symbolPairs.indexOf('/');
            if (split < 1) {
                log.error("????????????????????????????????????");
                return;
            }
            String base = symbolPairs.substring(0, split);
            String quote = symbolPairs.substring(split + 1);
            baseChoiceBox.setValue(base);
            quoteChoiceBox.setValue(quote);
            salebuyChoiceBox.setValue(tradeData.getSaleOrBuy());
            priceTextField.setText(tradeData.getPrice());
            numTextField.setText(tradeData.getBaseNum());
            totalTextField.setText(tradeData.getQuoteNum());
            dateDatePicker.setValue(DateHelper.fromString(tradeData.getDate()));
        } else {
            salebuyChoiceBox.setValue("???");
            priceTextField.setText("");
            numTextField.setText("");
            totalTextField.setText("");
            dateDatePicker.setValue(LocalDate.now());
        }
    }

    /**
     * @Description:
     * @return: boolean
     * @author: mapleaf
     * @date: 2020/6/23 16:56
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (baseChoiceBox.getValue() == null || baseChoiceBox.getValue().length() == 0) {
            errorMessage += "???????????????!\n";
        }
        if (quoteChoiceBox.getValue() == null || quoteChoiceBox.getValue().length() == 0) {
            errorMessage += "???????????????!\n";
        }
        if (salebuyChoiceBox.getValue() == null || salebuyChoiceBox.getValue().length() == 0) {
            errorMessage += "????????????/???!\n";
        }
        if (!DateHelper.validDate(DateHelper.toString(dateDatePicker.getValue()))
            || dateDatePicker.getValue() == null) {
            errorMessage += "???????????????!\n";
        }

        if (priceTextField.getText() == null || priceTextField.getText().length() == 0) {
            errorMessage += "???????????????!\n";
        } else {
            // try to parse the price into an double.
            try {
                Double.parseDouble(priceTextField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "???????????????(????????????????????????)!\n";
            }
        }

        if (numTextField.getText() == null || numTextField.getText().length() == 0) {
            errorMessage += "???????????????!\n";
        } else {
            // try to parse the num into an double.
            try {
                Double.parseDouble(numTextField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "???????????????(????????????????????????)!\n";
            }
        }

        if (totalTextField.getText() == null || totalTextField.getText().length() == 0) {
            errorMessage += "???????????????!\n";
        } else {
            // try to parse the total into an double.
            try {
                Double.parseDouble(totalTextField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "???????????????(????????????????????????)!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            workbench.showErrorDialog("??????", "???????????????", errorMessage, buttonType -> {
            });

            return false;
        }
    }


}
