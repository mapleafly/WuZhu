/*
 * Copyright 2019 xuelf.
 */
package org.lifxue.wuzhu.modules.piechart;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.lifxue.wuzhu.viewmodel.TypePieChartViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

@Slf4j
@Component
@FxmlView("TypePieChartView.fxml")
public class TypePieChartViewController implements Initializable {

    @FXML private PieChart pieChart;
    @FXML private Label totalPrice;

    private final TypePieChartViewModel viewModel;

    @Autowired
    public TypePieChartViewController(TypePieChartViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 先设置监听器，再加载数据
        viewModel.totalValueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                totalPrice.setText("当前总价值约:$" + Math.round(newVal.doubleValue()));
            }
        });
        
        // 设置初始值
        totalPrice.setText("当前总价值约:$0");
        
        viewModel.loadPortfolioData();
        pieChart.setData(viewModel.getPieData());

        setupTooltips();
    }

    private void setupTooltips() {
        double total = viewModel.getTotalValue().doubleValue();
        pieChart.getData().forEach(data -> {
            NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
            NumberFormat percent = NumberFormat.getPercentInstance();
            percent.setMaximumFractionDigits(3);

            Tooltip tooltip = new Tooltip(String.format("%s\n总价: %s\n占比: %s",
                data.getName(),
                currency.format(data.getPieValue()),
                percent.format(data.getPieValue() / total)));
            tooltip.setFont(new Font("Arial", 20));
            Tooltip.install(data.getNode(), tooltip);
        });
    }
}
