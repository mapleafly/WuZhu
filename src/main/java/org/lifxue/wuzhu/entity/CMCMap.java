package org.lifxue.wuzhu.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * @version 1.0
 * @classname CMCMap
 * @description 品种信息
 * @auhthor lifxue
 * @date 2023/1/6 22:03
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("cmc_map")
public class CMCMap implements Serializable {
    @TableId
    private Integer id;
    private String name;
    private String symbol;
    private String slug;
    private Integer isActive;
    private Integer rank;
    private String firstHistoricalData;
    private String lastHistoricalData;
    //private Platform platform;
    private Integer platformId;
    private String tokenAddress;

    //是否选中
    @Builder.Default
    private Integer isSelected = 0;

    @Override
    public boolean equals(Object o) {
        //先判断o是否为本对象，如果是就肯定是同一对象了，this 指向当前的对象
        if (this == o) {
            return true;
        }
        if (o instanceof CMCMap) {
            CMCMap bean = (CMCMap) o;
            //查看两个对象的id和is_active属性值是否相等,返回结果
            return Objects.equals(id, bean.id) && Objects.equals(isActive, bean.isActive);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        //hash = 89 * hash + Objects.hashCode(this.id);
        hash = 89 * hash + Objects.hashCode(this.id) + Objects.hashCode(this.isActive);
        return hash;
    }

}
