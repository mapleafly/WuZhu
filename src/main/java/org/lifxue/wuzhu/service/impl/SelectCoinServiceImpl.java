package org.lifxue.wuzhu.service.impl;

import lombok.extern.slf4j.Slf4j;
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
}
