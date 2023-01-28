package org.lifxue.wuzhu.service;

import org.lifxue.wuzhu.modules.selectcoin.vo.SelectDataVO;

import java.util.List;

/**
 * @version 1.0
 * @classname ISelectCoinService
 * @description TODO
 * @auhthor lifxue
 * @date 2023/1/9 22:33
 */
public interface ISelectCoinService {

    List<SelectDataVO> queryVO();
    List<SelectDataVO> queryVOBySymbol(String symbol);
}
