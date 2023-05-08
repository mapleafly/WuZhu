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
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.pojo.TradeInfoJpa;
import org.lifxue.wuzhu.service.ICMCMapJpaService;
import org.lifxue.wuzhu.service.ICMCQuotesLatestJpaService;
import org.lifxue.wuzhu.service.ITradeInfoJpaService;
import org.lifxue.wuzhu.util.CSVHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lif
 */
@Slf4j
@Component
public class ExportTradeData {

    private final Workbench workbench;

    private final ICMCMapJpaService icmcMapJpaService;

    private final ITradeInfoJpaService iTradeInfoJpaService;

    private final ICMCQuotesLatestJpaService icmcQuotesLatestJpaService;

    public ExportTradeData(
            Workbench workbench,
            ICMCMapJpaService icmcMapJpaService,
            ITradeInfoJpaService iTradeInfoJpaService,
            ICMCQuotesLatestJpaService icmcQuotesLatestJpaService
    ) {

        this.workbench = workbench;
        this.icmcMapJpaService = icmcMapJpaService;
        this.iTradeInfoJpaService = iTradeInfoJpaService;
        this.icmcQuotesLatestJpaService = icmcQuotesLatestJpaService;
    }

    public void handleExportData() {
        List<TradeInfoJpa> tradeInfoList = iTradeInfoJpaService.findOrderByTradeDate();
        String[] headers = {
            "id",
            "base_id",
            "base_symbol",
            "quote_id",
            "quote_symbol",
            "sale_or_buy",
            "price",
            "base_num",
            "quote_num",
            "trade_date"
        };
        if (tradeInfoList == null || tradeInfoList.isEmpty()) {
            workbench.showErrorDialog("错误", "导出数据失败！", "没有交易数据！", buttonType -> {
            });
            return;
        }
        List<String[]> data = new ArrayList<>();
        tradeInfoList.stream()
            .map(
                (bean) -> {
                    String[] str = new String[10];
                    str[0] = bean.getId().toString();
                    str[1] = bean.getBaseId().toString();
                    str[2] = bean.getBaseSymbol();
                    str[3] = bean.getQuoteId().toString();
                    str[4] = bean.getQuoteSymbol();
                    str[5] = bean.getSaleOrBuy();
                    str[6] = bean.getPrice();
                    str[7] = bean.getBaseNum();
                    str[8] = bean.getQuoteNum();
                    str[9] = bean.getTradeDate();
                    return str;
                })
            .forEachOrdered((str) -> data.add(str));
        FileChooser fileChooser = new FileChooser();
        // 文档类型过滤器
        FileChooser.ExtensionFilter extFilter =
            new FileChooser.ExtensionFilter("txt files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(workbench.getScene().getWindow());
        if (file != null) {
            if(CSVHelper.writeCsv(headers, data, file)){
                workbench.showInformationDialog("成功", "导出数据成功！", buttonType -> {
                });
            }else {
                workbench.showErrorDialog("错误", "导出数据失败！", buttonType -> {
                });
            }
        }
    }
}
