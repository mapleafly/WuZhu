package org.lifxue.wuzhu.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lifxue.wuzhu.modules.selectcoin.vo.SelectDataVO;
import org.lifxue.wuzhu.pojo.CMCMap;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.ISelectCoinService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName SelectCoinJpaServiceImpl
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 19:34
 * @Version 1.0
 */
@Slf4j
@Service
public class SelectCoinServiceImpl implements ISelectCoinService {
    private ICMCMapService icmcMapJpaService;

    public SelectCoinServiceImpl(ICMCMapService icmcMapJpaService) {
        this.icmcMapJpaService = icmcMapJpaService;
    }

    @Override
    public List<SelectDataVO> queryVO() {
        List<CMCMap> list = icmcMapJpaService.list();
        return CopyUtil.copySelectDataListJpa(list);
    }

    @Override
    public List<SelectDataVO> queryVOBySymbol(String symbol) {
        List<CMCMap> list = icmcMapJpaService.findBySymbolLikeOrderByTid(symbol);
        return CopyUtil.copySelectDataListJpa(list);
    }

    @Override
    public Boolean updateCheckStatus(@NotNull SelectDataVO selectDataVO) {
        List<CMCMap> list = icmcMapJpaService.getById(selectDataVO.getId());
        CMCMap cmcMap = (list == null || list.size() == 0 ) ? null : list.get(0);
        if (cmcMap != null) {
            cmcMap.setIsSelected(selectDataVO.getSelect() ? 1 : 0);

            return icmcMapJpaService.update(cmcMap);
        }
        return false;
    }
}
