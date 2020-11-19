package com.chenxiao.elasticsearch.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author ricky
 * @version V1.0
 * @Title: PaginationUtils
 * @Package: com.mountain.common.util
 * @Description: 分页参数处理工具
 * @date 2019/11/25
 */
public class PaginationUtils {

    /**
     * 分页参数转换
     *
     * @param paramsVo 分页参数
     * @return
     */
    public static <T> Page<T> fromGrid(PageParamsForm paramsVo) {
        return preResolve(
            paramsVo,
            pageParamsForm -> new Page<>(paramsVo.getPage(), paramsVo.getSize())
        );
    }

    /**
     * 分页参数转换
     *
     * @param paramsVo 分页参数
     * @return
     */
    public static Pageable of(PageParamsForm paramsVo) {
        return preResolve(
            paramsVo,
            pageParamsForm -> PageRequest.of(paramsVo.getPage(), paramsVo.getSize())
        );
    }

    private static <T> T preResolve(PageParamsForm paramsVo, Function<PageParamsForm, T> mapFun) {

        //        限制每页条数
        int size = paramsVo.getSize() > 100 ? 20 : paramsVo.getSize();

        Integer page = paramsVo.getPage();

        if (page == null || page < 0) {
            page = 1;
        }

        paramsVo.setSize(size);
        paramsVo.setPage(page);

        return mapFun.apply(paramsVo);

    }

    public static <T> Page<T> setRecords(Page page, List<T> records) {
        return page.setRecords(records);
    }

    public static <T> Page<T> setRecords(Page page, Supplier<List<T>> recordsFun) {
        if (page == null || page.getRecords().isEmpty()) {
            return page;
        }
        return page.setRecords(recordsFun.get());
    }

    public static <T, R> Page<R> mapPage(Page<T> page, Function<T, R> mapFun) {
        return setRecords(
            page, page.getRecords().stream().map(mapFun).filter(Objects::nonNull).collect(Collectors.toList())
        );

    }

    public static <R, ID, T> Page<T> setRecords(Page page, Function<R, ID> rIdFun, List<T> records, Function<T, ID> tIdFun) {

        if (records == null || records.isEmpty()) {
            return page.setRecords(Collections.emptyList());
        }

        Map<ID, T> rIdMap = records.stream()
            .collect(Collectors.toMap(tIdFun, Function.identity(), (k1, k2) -> k2));

        return mapPage(page, resource -> rIdMap.get(rIdFun.apply((R) resource)));

    }


}