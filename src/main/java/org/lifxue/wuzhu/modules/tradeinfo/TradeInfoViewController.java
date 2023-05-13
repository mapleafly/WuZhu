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
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.entity.TradeInfo;
import org.lifxue.wuzhu.modules.tradeinfo.vo.CoinChoiceBoxVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.TradeInfoJpa;
import org.lifxue.wuzhu.service.ITradeInfoJpaService;
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
    private final ObservableList<CoinChoiceBoxVO> coinChoiceBoxList;
    private List<CoinChoiceBoxVO> coinList;
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
    private ChoiceBox<CoinChoiceBoxVO> baseChoiceBox;
    @FXML
    private ChoiceBox<CoinChoiceBoxVO> quoteChoiceBox;
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
    private final ITradeInfoJpaService iTradeInfoJpaService;

    public TradeInfoViewController(ITradeInfoJpaService iTradeInfoJpaService) {
        this.iTradeInfoJpaService = iTradeInfoJpaService;
        tradeDataList = FXCollections.observableArrayList();
        coinChoiceBoxList = FXCollections.observableArrayList();
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
        //获取数据
        //coinList = iTradeInfoService.queryCurSymbol();
        coinList = iTradeInfoJpaService.queryCurCoin();
        if (coinList != null && !coinList.isEmpty()) {
            List<TradeInfoVO> tradeInfoList = iTradeInfoJpaService.queryTradeInfoByBaseCoinId(coinList.get(0).getCoinId());
            if(tradeInfoList != null && !tradeInfoList.isEmpty()) {
                this.tradeDataList.addAll(tradeInfoList);
            }
        }

        coinChoiceBoxList.addAll(coinList);

        dataTable.setItems(tradeDataList);

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        coinIdCol.setCellValueFactory(new PropertyValueFactory<>("coinId"));
        symbolPairsCol.setCellValueFactory(cellData -> cellData.getValue().symbolPairsProperty());
        salebuyCol.setCellValueFactory(cellData -> cellData.getValue().saleOrBuyProperty());
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        baseNumCol.setCellValueFactory(new PropertyValueFactory<>("baseNum"));
        quoteNumCol.setCellValueFactory(new PropertyValueFactory<>("quoteNum"));
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        //baseChoiceBox.setItems(FXCollections.observableArrayList(coinList));
        baseChoiceBox.setConverter(new StringConverter<CoinChoiceBoxVO>() {
            @Override
            public String toString(CoinChoiceBoxVO object) {
                return object.getSymbol();
            }

            @Override
            public CoinChoiceBoxVO fromString(String string) {
                return null;
            }
        });
        baseChoiceBox.setItems(coinChoiceBoxList);

        baseChoiceBox.setSelectionModel(new SingleSelectionModel<CoinChoiceBoxVO>(){
            @Override
            protected CoinChoiceBoxVO getModelItem(int index) {
                //return null;
                if(coinChoiceBoxList == null || coinChoiceBoxList.isEmpty()) {return null;}
                CoinChoiceBoxVO coinChoiceBoxVO = coinChoiceBoxList.get(index);
                return coinChoiceBoxVO;
            }

            @Override
            protected int getItemCount() {
                return coinChoiceBoxList == null ? 0 : coinChoiceBoxList.size();
            //return coinChoiceBoxList.size();
            }
        });

        baseChoiceBox.setTooltip(new Tooltip("选择基准货币"));
        baseChoiceBox.getSelectionModel().selectFirst();

        quoteChoiceBox.setConverter(new StringConverter<CoinChoiceBoxVO>() {
            @Override
            public String toString(CoinChoiceBoxVO object) {
                return object.getSymbol();
            }

            @Override
            public CoinChoiceBoxVO fromString(String string) {
                return null;
            }
        });

        CoinChoiceBoxVO quoteChoiceBoxVO = new CoinChoiceBoxVO("USDT",825);
        quoteChoiceBox.setItems(FXCollections.observableArrayList(quoteChoiceBoxVO));
        quoteChoiceBox.setSelectionModel(new SingleSelectionModel<CoinChoiceBoxVO>(){
            @Override
            protected CoinChoiceBoxVO getModelItem(int index) {
                return quoteChoiceBoxVO;
            }

            @Override
            protected int getItemCount() {
                return 1;
            }
        });
        quoteChoiceBox.setTooltip(new Tooltip("选择计价货币"));
        quoteChoiceBox.getSelectionModel().selectFirst();

        salebuyChoiceBox.setItems(FXCollections.observableArrayList("买", "卖"));
        salebuyChoiceBox.setTooltip(new Tooltip("选择交易类型"));

        dateDatePicker.setConverter(DateHelper.CONVERTER);
        // dateDatePicker.setPromptText(pattern.toLowerCase());
        dateDatePicker.setTooltip(new Tooltip("选择交易时间"));

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
                        CoinChoiceBoxVO selectedCoin = this.coinChoiceBoxList.get(newValue.intValue());
                        List<TradeInfoVO> tradeInfoVOS = iTradeInfoJpaService.queryTradeInfoByBaseCoinId(selectedCoin.getCoinId());
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
            TradeInfoJpa tradeInfo = new TradeInfoJpa();
            setTradeInfo(tradeInfo);
            if(iTradeInfoJpaService.save(tradeInfo)){
                tradeDataList.add(0, CopyUtil.copyjpa(tradeInfo));
                priceTextField.setText("");
                numTextField.setText("");
                totalTextField.setText("");
            }
        }
    }

    private void setTradeInfo(TradeInfoJpa tradeInfo) {
        tradeInfo.setBaseId(baseChoiceBox.getValue().getCoinId());
        tradeInfo.setBaseSymbol(baseChoiceBox.getValue().getSymbol());
        tradeInfo.setQuoteId(quoteChoiceBox.getValue().getCoinId());
        tradeInfo.setQuoteSymbol(quoteChoiceBox.getValue().getSymbol());
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
                workbench.showInformationDialog("提示", "没有选中数据", buttonType -> {
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
                log.error("交易对是空值或者格式错误");
                return;
            }
            String base = symbolPairs.substring(0, split);
            String quote = symbolPairs.substring(split + 1);
            baseChoiceBox.setValue(new CoinChoiceBoxVO(base,tradeData.getCoinId()));
            quoteChoiceBox.setValue(new CoinChoiceBoxVO("USDT", 825));
            salebuyChoiceBox.setValue(tradeData.getSaleOrBuy());
            priceTextField.setText(tradeData.getPrice());
            numTextField.setText(tradeData.getBaseNum());
            totalTextField.setText(tradeData.getQuoteNum());
            dateDatePicker.setValue(DateHelper.fromString(tradeData.getDate()));
        } else {
            salebuyChoiceBox.setValue("买");
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

        if (baseChoiceBox.getValue() == null) {
            errorMessage += "无效的类别!\n";
        }
        if (quoteChoiceBox.getValue() == null) {
            errorMessage += "无效的类别!\n";
        }
        if (salebuyChoiceBox.getValue() == null || salebuyChoiceBox.getValue().length() == 0) {
            errorMessage += "无效的买/卖!\n";
        }
        if (!DateHelper.validDate(DateHelper.toString(dateDatePicker.getValue()))
            || dateDatePicker.getValue() == null) {
            errorMessage += "无效的时间!\n";
        }

        if (priceTextField.getText() == null || priceTextField.getText().length() == 0) {
            errorMessage += "无效的单价!\n";
        } else {
            // try to parse the price into an double.
            try {
                Double.parseDouble(priceTextField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "无效的单价(必须是整数或小数)!\n";
            }
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

        if (totalTextField.getText() == null || totalTextField.getText().length() == 0) {
            errorMessage += "无效的总价!\n";
        } else {
            // try to parse the total into an double.
            try {
                Double.parseDouble(totalTextField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "无效的总价(必须是整数或小数)!\n";
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
