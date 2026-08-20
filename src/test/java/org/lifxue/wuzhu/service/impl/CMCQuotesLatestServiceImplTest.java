package org.lifxue.wuzhu.service.impl;

import org.junit.jupiter.api.Test;
import org.lifxue.wuzhu.convert.CMCQuotesLatestConvert;
import org.lifxue.wuzhu.pojo.CMCMap;
import org.lifxue.wuzhu.pojo.CMCQuotesLatest;
import org.lifxue.wuzhu.repository.CMCQuotesLatestRepository;
import org.lifxue.wuzhu.service.ICMCMapService;
import org.lifxue.wuzhu.service.feignc.ICMCQuotesLatestFeignClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CMCQuotesLatestServiceImpl 单元测试（纯 Mock，无需 GUI / 网络）。
 *
 * 覆盖「更新现价」菜单背后的核心逻辑 saveBatch()：
 *   1. 无选中币种 → 返回 false（UI 层弹「请检查网络/是否有关注币种」）
 *   2. 有选中币种 → 拼接 id 请求 API、转换并保存
 *   3. 网络异常 → 异常向上传播（由 UI 层 try/catch 捕获并弹窗）
 */
class CMCQuotesLatestServiceImplTest {

    private final ICMCQuotesLatestFeignClient feignClient = mock(ICMCQuotesLatestFeignClient.class);
    private final ICMCMapService icmcMapService = mock(ICMCMapService.class);
    private final CMCQuotesLatestRepository repository = mock(CMCQuotesLatestRepository.class);
    private final CMCQuotesLatestConvert convert = mock(CMCQuotesLatestConvert.class);

    private final CMCQuotesLatestServiceImpl service =
        new CMCQuotesLatestServiceImpl(feignClient, icmcMapService, repository, convert);

    private static final String JSON_TWO_COINS = """
        {"status":{"timestamp":"2026-01-01T00:00:00.000Z","error_code":0,"error_message":null},
         "data":{
            "1":{"id":1,"name":"Bitcoin","symbol":"BTC","slug":"bitcoin","last_updated":"2026-01-01T00:00:00.000Z",
                 "quote":{"USD":{"price":100000.0,"volume_24h":1.0,"market_cap":2.0}}},
            "1027":{"id":1027,"name":"Ethereum","symbol":"ETH","slug":"ethereum","last_updated":"2026-01-01T00:00:00.000Z",
                    "quote":{"USD":{"price":5000.0,"volume_24h":1.0,"market_cap":2.0}}}
         }}
        """;

    private static CMCMap selected(int tid) {
        return CMCMap.builder().tid(tid).isSelected(1).build();
    }

    @Test
    void saveBatch_noSelectedCoins_returnsFalse() {
        when(icmcMapService.list(1)).thenReturn(List.of());
        assertFalse(service.saveBatch(), "无选中币种应返回 false");
        verify(feignClient, never()).getHttpJsonById(anyString(), anyString(), anyString());
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void saveBatch_withSelectedCoins_requestsByIdsAndSaves() {
        when(icmcMapService.list(1)).thenReturn(List.of(selected(1), selected(1027)));
        when(feignClient.getHttpJsonById(anyString(), anyString(), anyString())).thenReturn(JSON_TWO_COINS);
        when(convert.convertList(anyList())).thenAnswer(inv -> List.of(new CMCQuotesLatest(), new CMCQuotesLatest()));
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = service.saveBatch();

        assertTrue(result, "有选中币种且 API 正常时应保存并返回 true");
        // 请求的 id 串应包含两个选中币种，convert 为 USD
        verify(feignClient, times(1)).getHttpJsonById(eq("1,1027"), eq("USD"), anyString());
        verify(repository, times(1)).saveAll(anyList());
    }

    @Test
    void saveBatch_networkError_propagatesException() {
        when(icmcMapService.list(1)).thenReturn(List.of(selected(1)));
        when(feignClient.getHttpJsonById(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("(unexpected_message) Received close_notify during handshake"));

        assertThrows(RuntimeException.class, () -> service.saveBatch(),
            "网络异常应向上传播，由 UI 层 try/catch 捕获并弹出异常窗口");
    }

    @Test
    void saveBatch_apiErrorCode_returnsNullThenFalse() {
        when(icmcMapService.list(1)).thenReturn(List.of(selected(1)));
        String errorJson = """
            {"status":{"timestamp":"2026-01-01T00:00:00.000Z","error_code":1001,"error_message":"API key invalid"}}
            """;
        when(feignClient.getHttpJsonById(anyString(), anyString(), anyString())).thenReturn(errorJson);
        when(convert.convertList(anyList())).thenReturn(List.of());

        boolean result = service.saveBatch();
        assertFalse(result, "API 返回错误码时不应保存");
        verify(repository, never()).saveAll(anyList());
    }
}
