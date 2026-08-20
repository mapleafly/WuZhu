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
        // 重新打开页面时，如果总价值与上次相同，JavaFX 属性监听器不会触发，
        // 这里直接根据当前值刷新标签，避免第二次打开显示 0
        totalPrice.setText("当前总价值约:$" + Math.round(viewModel.getTotalValue().doubleValue()));
        pieChart.setData(viewModel.getPieData());

        setupTooltips();
    }

    private void setupTooltips() {
        double total = viewModel.getTotalValue().doubleValue();
        pieChart.getData().forEach(data -> {
            NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
            NumberFormat percent = NumberFormat.getPercentInstance();
            percent.setMaximumFractionDigits(3);

            Tooltip tooltip = new Tooltip(String.format("%s\n总价: %s\n占比: %s\n数量: %s",
                data.getName(),
                currency.format(data.getPieValue()),
                percent.format(data.getPieValue() / total),
                formatQuantity(viewModel.getPieDataQuantity(data))));
            tooltip.setFont(new Font("Arial", 20));
            Tooltip.install(data.getNode(), tooltip);
        });
    }

    /**
     * 格式化持仓数量，去掉多余的尾随零，最多保留 8 位小数
     */
    private String formatQuantity(java.math.BigDecimal quantity) {
        if (quantity == null) {
            return "0";
        }
        java.math.BigDecimal scaled = quantity.setScale(8, java.math.RoundingMode.HALF_UP).stripTrailingZeros();
        return scaled.toPlainString();
    }
}
