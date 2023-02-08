package org.lifxue.wuzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lifxue.wuzhu.entity.CMCMap;
import org.lifxue.wuzhu.modules.selectcoin.vo.SelectDataVO;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.ISelectCoinService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @version 1.0
 * @classname SelectCoinServiceImpl
 * @description TODO
 * @auhthor lifxue
 * @date 2023/1/9 22:36
 */
@Slf4j
@Component
public class SelectCoinServiceImpl implements ISelectCoinService {

    private ICMCMapService icmcMapService;

    @Autowired
    public SelectCoinServiceImpl(ICMCMapService icmcMapService){
        this.icmcMapService = icmcMapService;
    }
    @Override
    public List<SelectDataVO> queryVO() {
        List<CMCMap> list = icmcMapService.list();
        return CopyUtil.copyList(list);
    }

    @Override
    public List<SelectDataVO> queryVOBySymbol(String symbol) {
        QueryWrapper<CMCMap> wrapper = new QueryWrapper<>();
        wrapper.like("symbol",symbol).or().like("symbol",symbol.toUpperCase());
        List<CMCMap> list = icmcMapService.list(wrapper);
        return CopyUtil.copyList(list);
    }

    @Override
    public Boolean updateCheckStatus( @NotNull SelectDataVO selectDataVO) {
        CMCMap cmcMap = icmcMapService.getById(selectDataVO.getId());
        if(cmcMap != null){
            cmcMap.setIsSelected(selectDataVO.getSelect() ? 1 : 0);

            return icmcMapService.updateById(cmcMap);
        }
        return false;
    }
}
