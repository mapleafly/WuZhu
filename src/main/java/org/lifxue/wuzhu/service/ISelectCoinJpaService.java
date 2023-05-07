package org.lifxue.wuzhu.service;

import org.jetbrains.annotations.NotNull;
import org.lifxue.wuzhu.modules.selectcoin.vo.SelectDataVO;

import java.util.List;

/**
 * @ClassName ISelectCoinJpaService
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 19:33
 * @Version 1.0
 */
public interface ISelectCoinJpaService {
    List<SelectDataVO> queryVO();
    List<SelectDataVO> queryVOBySymbol(String symbol);

    Boolean updateCheckStatus(@NotNull SelectDataVO selectDataVO);
}
