package org.lifxue.wuzhu.pojo;


import lombok.*;


import javax.persistence.*;
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
@Entity
@Table(name = "cmc_map_jpa")
public class CMCMapJpa implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;
    //coin or token id
    private Integer tid;
    private String name;
    private String symbol;
    private String slug;
    @Column(name = "IS_ACTIVE" )
    private Integer isActive;
    private Integer rank;
    @Column(name = "FIRST_HISTORICAL_DATA")
    private String firstHistoricalData;
    @Column(name = "LAST_HISTORICAL_DATA")
    private String lastHistoricalData;
    //private Platform platform;
    @Column(name = "PLATFORM_ID")
    private Integer platformId;
    @Column(name = "TOKEN_ADDRESS")
    private String tokenAddress;

    //是否选中
    @Builder.Default
    @Column(name = "IS_SELECTED")
    private Integer isSelected = 0;

    @Override
    public boolean equals(Object o) {
        //先判断o是否为本对象，如果是就肯定是同一对象了，this 指向当前的对象
        if (this == o) {
            return true;
        }
        if (o instanceof CMCMapJpa) {
            CMCMapJpa bean = (CMCMapJpa) o;
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
