package com.yunzen.jijuaner.fund.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.yunzen.jijuaner.common.to.FundSimpleAndRealTimeInfoTo;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.entity.FundInfoEntity;
import com.yunzen.jijuaner.fund.entity.FundRealTimeInfoEntity;
import com.yunzen.jijuaner.fund.service.FundInfoService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fund/fundInfo")
public class FundInfoController {
    @Autowired
    private FundInfoService fundInfoService;

    @RequestMapping("/hello")
    public String hello() {
        return "hello, fund info!!!";
    }

    @GetMapping("/{id}")
    public R getInfo(@PathVariable("id") String id) {
        FundInfoEntity info = fundInfoService.getInfoById(id);
        return R.ok().putData(info);
    }

    /**
     * 获取基金的简单信息, 包括基金代码, 基金名字, 基金类型, 近一年收益率, 近六个月收益率, 近三个月收益率, 近一个月收益率
     * <p>
     * R 中的 data 对象: {@link FundInfoEntity}
     */
    @GetMapping("/simple/{id}")
    public R getSimpleInfo(@PathVariable("id") String id) {
        FundInfoEntity info = fundInfoService.getSimpleInfoById(id);
        return R.ok().putData(info);
    }

    /**
     * 获取基金实时数据, 包括基金代码, 基金名称, 单位净值, 累计净值, 实时估值, 实时估计涨跌幅, 估值日期
     * @return R 中的 data 对象: {@link FundRealTimeInfoEntity}
     */
    @GetMapping("/realTime/{id}")
    public R getRealTimeInfo(@PathVariable("id") String id) {
        FundRealTimeInfoEntity info = fundInfoService.getRealTimeInfoById(id);
        return R.ok().putData(info);
    }

    @Autowired
    private ThreadPoolExecutor executor;

    /**
     * 获取基金的简单信息 + 实时数据
     * @return R 中的 data 对象: List<{@link FundSimpleAndRealTimeInfoTo}>
     */
    @PostMapping("/simpleAndRealTime")
    public R getSimpleAndRealTimeInfos(@RequestBody List<String> ids) throws InterruptedException, ExecutionException {
        // 开启两个任务分别获取简单信息和实时数据
        CompletableFuture<List<FundInfoEntity>> simpleInfosFuture = CompletableFuture.supplyAsync(() -> {
            return ids.stream().parallel().map(fundInfoService::getSimpleInfoById)
                    .toList();
        }, executor);
        CompletableFuture<List<FundRealTimeInfoEntity>> realTimeInfosFuture = CompletableFuture.supplyAsync(() -> {
            return ids.stream().parallel().map(fundInfoService::getRealTimeInfoById)
                    .toList();
        }, executor);
        // 等待所有的异步任务全部完成
        List<FundInfoEntity> simpleInfos = simpleInfosFuture.get();
        List<FundRealTimeInfoEntity> realTimeInfos = realTimeInfosFuture.get();

        var tos = new ArrayList<FundSimpleAndRealTimeInfoTo>();
        for (int i = 0; i < ids.size(); ++i) {
            var to = new FundSimpleAndRealTimeInfoTo();
            BeanUtils.copyProperties(realTimeInfos.get(i), to);
            BeanUtils.copyProperties(simpleInfos.get(i), to);
            tos.add(to);
        }
        return R.ok().putData(tos);
    }

}
