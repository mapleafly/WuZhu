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
package org.lifxue.wuzhu.modules.file;

import com.dlsc.workbenchfx.Workbench;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.ICMCQuotesLatestService;
import org.lifxue.wuzhu.service.ITradeInfoService;
import org.lifxue.wuzhu.util.CSVHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author lif
 */

@Slf4j
@Component
public class ImportTradeData {

    private final Workbench workbench;

    private final ICMCMapService icmcMapService;

    private final ITradeInfoService iTradeInfoService;

    private final ICMCQuotesLatestService icmcQuotesLatestService;



    @Autowired
    public ImportTradeData(Workbench workbench,
                           ICMCMapService icmcMapService,
                           ITradeInfoService iTradeInfoService,
                           ICMCQuotesLatestService icmcQuotesLatestService
    ) {

        this.workbench = workbench;
        this.icmcMapService = icmcMapService;
        this.iTradeInfoService = iTradeInfoService;
        this.icmcQuotesLatestService = icmcQuotesLatestService;
    }

    public void handleImportData() {
        workbench.showConfirmationDialog(
            "导入数据操作",
            "这个操作会覆盖原有数据！你确定要导入新数据吗？",
            buttonType -> {
                if (buttonType == ButtonType.YES) {
                    importData();
                }
            });
    }

    private void importData() {
        FileChooser fileChooser = new FileChooser();
        // 文档类型过滤器
        FileChooser.ExtensionFilter extFilter =
            new FileChooser.ExtensionFilter("txt files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(workbench.getScene().getWindow());
        if (file != null) {
            List<String[]> list = CSVHelper.readCsv(file.toString());
            if (list.isEmpty()) {
                workbench.showErrorDialog("错误", "导入数据失败！", "csv文件为空，或者交易数据格式错误！", buttonType -> {
                });
            } else {
                // 更新可使用品种数据
                List<Integer> coinid = new ArrayList<>();
                for (String[] str : list) {
                    coinid.add(Integer.parseInt(str[1]));
                }
                // 去重
                LinkedHashSet<Integer> hashSet = new LinkedHashSet<>(coinid);
                List<Integer> listWithoutDuplicates = new ArrayList<>(hashSet);

                List<Integer> usedid = icmcMapService.getSelectedIDs();
                // 差集 (list1 - list2)
                listWithoutDuplicates.removeAll(usedid);
                // 将csv文件中的可用类型和数据库中的可用类型做差集后，将差集更新进数据库中
                if (listWithoutDuplicates.size() > 0) {
                    icmcMapService.updateSelectedBatch(listWithoutDuplicates);
                }

                // 删除数据库中的原有数据
                iTradeInfoService.truncate();
                // 导入到数据库
                if(!iTradeInfoService.saveBatch(list)){
                    //导入失败
                    workbench.showErrorDialog("错误", "导入数据失败！", "导入失败！", buttonType -> {});
                    return;
                }

                // 导入数据后自动更新价格
                icmcQuotesLatestService.saveBatch();
                workbench.showInformationDialog(
                    "完成导入",
                    "数据导入行数：" + list.size() + ";\n",
                    buttonType -> {
                    });
            }
        }
    }
}
