package com.chenxiao.elasticsearch.common;

import com.alibaba.fastjson.JSONObject;
import com.chenxiao.elasticsearch.entity.Book;
import com.chenxiao.elasticsearch.utils.LambdaUtil;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @name:
 * @Author: cx
 * @date: 2020/11/19
 * @describe: 结果集格式化
 */
@Component
public class SearchItemBuilder {

    public Map<Integer, Object> build(SearchHit[] hits) {
        Map<String, List<SearchHit>> hitMap = Arrays.stream(hits).collect(
            Collectors.groupingBy(
                SearchHit::getIndex, Collectors.toList()
            )
        );
        Map<Integer, Object> resultMap = new HashMap<>(hits.length);

        Map<Integer, Book> bookMap = resolve(hitMap, "dubang_book", Book.class);
        List<Book> collect = new ArrayList<>(bookMap.values());
        Map<Integer, Book> bookItemMap = LambdaUtil.toMap(
            collect,
            Book::getId
        );
        resultMap.putAll(
            bookMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> bookItemMap.get(entry.getValue().getId()),
                LambdaUtil.replaceMergeFunction()
            ))
        );
        return resultMap;

    }
    /**
     * 格式化转换成对应的实体
     * @param hits 查询结果
     * @param entityClass 对应实体
     * @param <T>
     * @return
     */
    public <T> List<T> build(SearchHit[] hits, Class<T> entityClass) {
        return Stream.of(hits)
            .map(hit -> JSONObject.parseObject(hit.getSourceAsString(), entityClass))
            .collect(Collectors.toList());
    }
    //    根据索引 解析成实体
    private <T> Map<Integer, T> resolve(Map<String, List<SearchHit>> hitMap, String indexName, Class<T> entityClass) {
        List<SearchHit> searchHitList = hitMap.get(indexName);
        if (searchHitList == null || searchHitList.isEmpty()) {
            return new HashMap<>();
        }
        return searchHitList.stream()
            .collect(
                Collectors.toMap(
                    SearchHit::hashCode,
                    hit -> JSONObject.parseObject(hit.getSourceAsString(), entityClass),
                    LambdaUtil.replaceMergeFunction()
                )
            );
    }
}
