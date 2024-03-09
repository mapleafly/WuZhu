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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
    private final ISelectCoinService iSelectCoinJpaService;


    public SelectCoinViewController(ISelectCoinService iSelectCoinJpaService) {
        this.iSelectCoinJpaService = iSelectCoinJpaService;
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
        List<SelectDataVO> list = iSelectCoinJpaService.queryVO();
        if (list == null){
            list = new ArrayList<>();
        }
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
                                        if (!iSelectCoinJpaService.updateCheckStatus(svo)) {
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

        selectCol.setCellValueFactory(new PropertyValueFactory<>("select"));
        selectCol.setEditable(true);

        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        symbolCol.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        rankCol.setCellValueFactory(cellData -> cellData.getValue().rankProperty());
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());


    }

    @FXML
    private void handleSearchFieldKeyReleased(KeyEvent event) {
        coinTypeData.clear();
        List<SelectDataVO> list = iSelectCoinJpaService.queryVOBySymbol(searchField.getText().trim().toUpperCase());
        if (list != null && !list.isEmpty()) {
            coinTypeData.addAll(list);
        }
    }


}
