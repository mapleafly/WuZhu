package org.lifxue.wuzhu.service.impl;

import org.junit.jupiter.api.Test;
import org.lifxue.wuzhu.exception.ApiCallException;
import org.lifxue.wuzhu.pojo.CMCMap;
import org.lifxue.wuzhu.repository.CMCMapRepository;
import org.lifxue.wuzhu.service.feignc.ICMCMapFeignClient;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CMCMapServiceImpl 单元测试（纯 Mock，无需 GUI / 网络）。
 *
 * 覆盖「更新货币数据」菜单背后的核心逻辑 saveNewBatch：
 *   1. 网络正常 + 有新币种 → 新增并返回 true
 *   2. 网络正常 + 无新币种 → 返回 true，不写库
 *   3. 网络异常（Feign 抛错）→ 异常向上传播（由 UI 层的 try/catch 捕获并弹窗）
 */
class CMCMapServiceImplTest {

    private final CMCMapRepository cmcMapRepository = mock(CMCMapRepository.class);
    private final ICMCMapFeignClient feignClient = mock(ICMCMapFeignClient.class);
    private final CMCMapServiceImpl service = new CMCMapServiceImpl(feignClient, cmcMapRepository);

    private static final String JSON_TWO_COINS = """
        {"status":{"timestamp":"2026-01-01T00:00:00.000Z","error_code":0,"error_message":null,"elapsed":10,"credit_count":1},
         "data":[
            {"id":1,"name":"Bitcoin","symbol":"BTC","slug":"bitcoin","rank":1,"is_active":1,"first_historical_data":"2013-04-28T00:00:00.000Z","last_historical_data":"2026-01-01T00:00:00.000Z"},
            {"id":1027,"name":"Ethereum","symbol":"ETH","slug":"ethereum","rank":2,"is_active":1,"first_historical_data":"2015-08-07T00:00:00.000Z","last_historical_data":"2026-01-01T00:00:00.000Z"}
         ]}
        """;

    private static CMCMap coin(int tid) {
        return CMCMap.builder().tid(tid).symbol("C" + tid).build();
    }

    @Test
    void saveNewBatch_withNewCoins_savesOnlyNewAndReturnsTrue() {
        // API 返回 BTC + ETH（一次请求即拿完，limit 5000 返回 2 条 < 5000 停止循环）
        when(feignClient.getHttpJson(anyInt(), anyInt(), eq("cmc_rank"))).thenReturn(JSON_TWO_COINS);
        // 数据库已有 BTC（tid=1）
        when(cmcMapRepository.findAll()).thenReturn(List.of(coin(1)));
        when(cmcMapRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = service.saveNewBatch("cmc_rank");

        assertTrue(result, "有新币种时应保存并返回 true");
        // 应保存 ETH（tid=1027），不重复保存 BTC
        @SuppressWarnings("unchecked")
        List<CMCMap> saved = (List<CMCMap>) cmcMapRepository.saveAll(anyList());
        verify(cmcMapRepository, times(1)).saveAll(argThat(list -> {
            List<?> l = (List<?>) list;
            return l.size() == 1 && ((CMCMap) l.get(0)).getTid() == 1027;
        }));
    }

    @Test
    void saveNewBatch_noNewCoins_returnsTrueWithoutSaving() {
        when(feignClient.getHttpJson(anyInt(), anyInt(), eq("cmc_rank"))).thenReturn(JSON_TWO_COINS);
        // 数据库已有全部币种
        when(cmcMapRepository.findAll()).thenReturn(Arrays.asList(coin(1), coin(1027)));

        boolean result = service.saveNewBatch("cmc_rank");

        assertTrue(result, "无新币种时应返回 true（表示成功）");
        verify(cmcMapRepository, never()).saveAll(anyList());
    }

    @Test
    void saveNewBatch_networkError_propagatesExceptionForUiToShowPopup() {
        // 模拟 Feign 调用失败（正是 v1.0.4 的 TLS 握手异常路径）
        when(feignClient.getHttpJson(anyInt(), anyInt(), eq("cmc_rank")))
            .thenThrow(new ApiCallException("(unexpected_message) Received close_notify during handshake"));

        // UI 层的 coinMapItem() 已加 try/catch：这里只需断言异常能向上传播
        assertThrows(ApiCallException.class, () -> service.saveNewBatch("cmc_rank"));
    }

    @Test
    void saveNewBatch_invalidApiResponse_propagatesException() {
        // API 返回不含 data 字段的 JSON（如限流/异常响应）→ jsonToDto 抛 ApiCallException，
        // 由 UI 层（coinMapItem 的 try/catch）捕获并弹窗，而不是"静默无反应"。
        when(feignClient.getHttpJson(anyInt(), anyInt(), eq("cmc_rank"))).thenReturn("{}");
        assertThrows(ApiCallException.class, () -> service.saveNewBatch("cmc_rank"));
    }
}
