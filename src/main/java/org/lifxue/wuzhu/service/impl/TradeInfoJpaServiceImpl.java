package org.lifxue.wuzhu.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lifxue.wuzhu.modules.tradeinfo.vo.CoinChoiceBoxVO;
import org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO;
import org.lifxue.wuzhu.pojo.CMCMapJpa;
import org.lifxue.wuzhu.pojo.TradeInfoJpa;
import org.lifxue.wuzhu.repository.TradeInfoRepository;
import org.lifxue.wuzhu.service.ICMCMapJpaService;
import org.lifxue.wuzhu.service.ITradeInfoJpaService;
import org.lifxue.wuzhu.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName TradeInfoJpaServiceImpl
 * @Description TODO
 * @Auhthor lifxu
 * @Date 2023/5/7 16:12
 * @Version 1.0
 */
@Slf4j
@Service
public class TradeInfoJpaServiceImpl implements ITradeInfoJpaService {
    private ICMCMapJpaService icmcMapJpaService;
    private TradeInfoRepository tradeInfoRepository;


    @Autowired
    public void setIcmcMapJpaService(ICMCMapJpaService icmcMapJpaService) {
        this.icmcMapJpaService = icmcMapJpaService;
    }
    @Autowired
    public void setTradeInfoRepository(TradeInfoRepository tradeInfoRepository) {
        this.tradeInfoRepository = tradeInfoRepository;
    }
    /***
     * @description 查询可用coin的symbo集合
     * @author lifxue
     * @date 2023/2/12 17:59
     * @param
     * @return java.util.List<java.lang.String>
     **/
    @Override
    public List<String> queryCurSymbol() {
        return icmcMapJpaService.queryCurSymbol();
    }

    @Override
    public List<CoinChoiceBoxVO> queryCurCoin() {
        List<CMCMapJpa> cmcMaps = icmcMapJpaService.getSelecteds();
        return CopyUtil.copyCoinChoiceBoxVOListJpa(cmcMaps);
    }

    /***
     * @description 查询指定的币种信息并转变成TradeInfoVO列表格式
     * @author lifxue
     * @date 2023/2/12 18:38
     * @param symbol
     * @return java.util.List<org.lifxue.wuzhu.modules.tradeinfo.vo.TradeInfoVO>
     **/
    @Override
    public List<TradeInfoVO> queryTradeInfoByBaseSymbol(String symbol) {
        List<TradeInfoJpa> tradeInfoList = tradeInfoRepository.findByBaseSymbolOrderByIdDesc(symbol);
        return CopyUtil.copyTradeInfoVOListJpa(tradeInfoList);
    }

    @Override
    public List<TradeInfoVO> queryTradeInfoByBaseCoinId(Integer coinId) {
        List<TradeInfoJpa> tradeInfoList = tradeInfoRepository.findByBaseIdOrderByIdDesc(coinId);
        return CopyUtil.copyTradeInfoVOListJpa(tradeInfoList);
    }

    @Override
    public CMCMapJpa queryCoinBySymbol(String symbol) {
        return icmcMapJpaService.queryCoinBySymbo(symbol);
    }

    @Override
    public void truncate() {
        tradeInfoRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public boolean saveBatch(List<String[]> list) {
        List<TradeInfoJpa> tradeInfoList = CopyUtil.copyTradeInfoListJpa(list);
        return tradeInfoRepository.saveAll(tradeInfoList) == null ? false : true;
    }

    @Override
    public List<TradeInfoJpa> findOrderByTradeDate() {
        Sort sort = Sort.by(Sort.Direction.DESC, "tradeDate");
        return tradeInfoRepository.findAll(sort);
    }

    @Override
    public List<TradeInfoJpa> findByTradeDateBetweenOrderByTradeDateDesc(String startDate, String endDate) {
        return tradeInfoRepository.findByTradeDateBetweenOrderByTradeDateDesc(startDate, endDate);
    }

    @Override
    public boolean save(TradeInfoJpa tradeInfoJpa) {
        return tradeInfoRepository.save(tradeInfoJpa) == null ? false : true;
    }

    @Override
    public TradeInfoJpa findById(Integer id) {
        return tradeInfoRepository.getReferenceById(id);
    }

    @Override
    public boolean deleteById(Integer id) {
        tradeInfoRepository.deleteById(id);
        return true;
    }

}
