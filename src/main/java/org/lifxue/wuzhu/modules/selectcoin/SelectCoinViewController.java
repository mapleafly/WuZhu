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
package org.lifxue.wuzhu.modules.selectcoin;

import com.dlsc.workbenchfx.Workbench;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.modules.selectcoin.vo.SelectDataVO;
import org.lifxue.wuzhu.service.ISelectCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;

/**
 * FXML Controller class
 *
 * @author xuelf
 */
@Slf4j
@Component
@FxmlView("SelectCoinView.fxml")
public class SelectCoinViewController implements Initializable {
    private final ObservableList<SelectDataVO> coinTypeData;
    @FXML
    private TableView<SelectDataVO> priceTable;
    @FXML
    private TableColumn<SelectDataVO, Number> idCol;
    @FXML
    private TableColumn<SelectDataVO, Boolean> selectCol;
    @FXML
    private TableColumn<SelectDataVO, String> nameCol;
    @FXML
    private TableColumn<SelectDataVO, String> symbolCol;
    @FXML
    private TableColumn<SelectDataVO, Number> rankCol;
    @FXML
    private TableColumn<SelectDataVO, String> dateCol;
    @FXML
    private TextField searchField;

    private Workbench workbench;
    private final ISelectCoinService iSelectCoinService;


    @Autowired
    public SelectCoinViewController(ISelectCoinService iSelectCoinService) {
        this.iSelectCoinService = iSelectCoinService;
        this.coinTypeData = FXCollections.observableArrayList();
    }

    /**
     * @param workbench 1
     * @Description: 装配workbench
     * @return: void
     * @author: mapleaf
     * @date: 2020/6/23 18:51
     */
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
     * @date: 2020/6/23 18:49
     */
    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        coinTypeData.clear();
        //获取数据
        List<SelectDataVO> list = iSelectCoinService.queryVO();
        coinTypeData.addAll(list);
        priceTable.setItems(coinTypeData);


        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());

        selectCol.setCellFactory(new Callback<TableColumn<SelectDataVO, Boolean>, TableCell<SelectDataVO, Boolean>>() {

            @Override
            public TableCell<SelectDataVO, Boolean> call(TableColumn<SelectDataVO, Boolean> param) {
                TableCell<SelectDataVO, Boolean> cell = new TableCell<SelectDataVO, Boolean>() {
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty == false && item != null) {
                            HBox hbox = new HBox();
                            hbox.setAlignment(Pos.CENTER);
                            hbox.prefWidthProperty().bind(this.getTableColumn().widthProperty());
                            CheckBox checkBox = new CheckBox();
                            checkBox.setSelected(item);
                            if (this.getTableRow() != null) {
                                ObservableList<SelectDataVO> items = this.getTableView().getItems();
                                SelectDataVO svo = items.get(this.getTableRow().getIndex());
                                //checkBox.selectedProperty().bindBidirectional(svo.selectProperty());
                                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                    if (newValue != oldValue) {
                                        svo.setSelect(newValue);
                                        if (!iSelectCoinService.updateCheckStatus(svo)) {
                                            workbench.showErrorDialog("错误", "选择操作没有保存成功！", buttonType -> {
                                            });
                                        }
                                    }
                                });
                            }


                            hbox.getChildren().add(checkBox);
                            this.setGraphic(hbox);
                        }
                    }
                };
                return cell;
            }
        });
        //selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(new Callback<Integer, ObservableValue<Boolean>>(){
        //
        //    @Override
        //    public ObservableValue<Boolean> call(Integer param) {
        //
        //        System.out.println("getSelect "+coinTypeData.get(param).getSymbol()+" changed value to " +coinTypeData.get(param).getSelect());
        //        return coinTypeData.get(param).selectProperty();
        //
        //    }
        //}));
        selectCol.setCellValueFactory(new PropertyValueFactory<>("select"));
        selectCol.setEditable(true);

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        symbolCol.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        rankCol.setCellValueFactory(cellData -> cellData.getValue().rankProperty());
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());


    }

 /*   @FXML
    private void handleSave(ActionEvent event) {
        //todo  窗口表格中按id 和 排名排序有问题，不是按数字来排名的，好像是按字符串方式排名的
        List<Integer> dbCurId = CoinTypeDao.queryCurID();
        Map<Integer, Integer> tableSelectedMap = new HashMap<>();
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < priceTable.getItems().size(); i++) {
            items.add(Integer.valueOf(priceTable.getItems().get(i).getId()));
            if (priceTable.getItems().get(i).getSelect()) {
                tableSelectedMap.put(Integer.valueOf(priceTable.getItems().get(i).getId()), 1);
            }
        }
        // 交集-dbCurId留下items中也存在的项
        dbCurId.retainAll(items);

        dbCurId.forEach(
            (id) -> {
                if (tableSelectedMap.containsKey(id)) {
                    tableSelectedMap.remove(id);
                } else {
                    tableSelectedMap.put(id, 0);
                }
            });
        if (CoinTypeDao.batchUpdate(tableSelectedMap) == tableSelectedMap.size()) {
            // 自动更新选择coin的当前价格
            CoinInfo info = new CoinInfo(workbench,null);
            info.updateCurPrice();

            workbench.showInformationDialog("信息", "完成可用品种保存操作！", buttonType -> {
            });
        }
    }
*/
    @FXML
    private void handleSearchFieldKeyReleased(KeyEvent event) {
        coinTypeData.clear();
        List<SelectDataVO> list = iSelectCoinService.queryVOBySymbol(searchField.getText().trim());
        if (list != null && !list.isEmpty()) {
            coinTypeData.addAll(list);
        }
    }


}
