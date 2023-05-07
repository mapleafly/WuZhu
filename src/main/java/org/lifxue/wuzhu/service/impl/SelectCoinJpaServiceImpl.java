package org.lifxue.wuzhu.service.impl;

import org.jetbrains.annotations.NotNull;
import org.lifxue.wuzhu.modules.selectcoin.vo.SelectDataVO;
import org.lifxue.wuzhu.pojo.CMCMapJpa;
import org.lifxue.wuzhu.service.ICMCMapJpaService;
import org.lifxue.wuzhu.service.ISelectCoinJpaService;
import org.lifxue.wuzhu.util.CopyUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SelectCoinJpaServiceImpl
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 19:34
 * @Version 1.0
 */
public class SelectCoinJpaServiceImpl implements ISelectCoinJpaService {
    private ICMCMapJpaService icmcMapJpaService;

    public SelectCoinJpaServiceImpl(ICMCMapJpaService icmcMapJpaService) {
        this.icmcMapJpaService = icmcMapJpaService;
    }

    @Override
    public List<SelectDataVO> queryVO() {
        List<CMCMapJpa> list = icmcMapJpaService.list();
        return CopyUtil.copySelectDataListJpa(list);
    }

    @Override
    public List<SelectDataVO> queryVOBySymbol(String symbol) {
        List<CMCMapJpa> list = new ArrayList<>();
        list.add(icmcMapJpaService.queryCoinBySymbo(symbol));
        return CopyUtil.copySelectDataListJpa(list);
    }

    @Override
    public Boolean updateCheckStatus(@NotNull SelectDataVO selectDataVO) {
        List<CMCMapJpa> list = icmcMapJpaService.getById(selectDataVO.getId());
        CMCMapJpa cmcMap = (list == null || list.size() == 0 ) ? null : list.get(0);
        if (cmcMap != null) {
            cmcMap.setIsSelected(selectDataVO.getSelect() ? 1 : 0);

            return icmcMapJpaService.update(cmcMap);
        }
        return false;
    }
}
