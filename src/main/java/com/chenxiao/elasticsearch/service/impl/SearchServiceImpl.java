package com.chenxiao.elasticsearch.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chenxiao.elasticsearch.common.SearchItemBuilder;
import com.chenxiao.elasticsearch.enums.SearchType;
import com.chenxiao.elasticsearch.service.SearchService;
import com.chenxiao.elasticsearch.utils.LambdaUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @name:
 * @Author: cx
 * @date: 2020/11/19
 * @describe:
 */
@Service
public class SearchServiceImpl implements SearchService {

    private Map<SearchType, Set<String>> searchIndexMap = new ConcurrentReferenceHashMap<>(5);

    private Map<SearchType, Set<String>> searchFieldMap = new ConcurrentReferenceHashMap<>(5);

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    private SearchItemBuilder searchItemBuilder;

    /**
     * es查询工具
     *
     * @param searchWords 搜索字段与搜索值
     * @param entityClass 返回类型
     * @param indexName   文档库
     * @param pageable    页码
     * @param <T>
     * @return
     */
    @Override
    public <T> Page<T> search(Map<String, Map<String, String>> searchWords, String indexName, Class<T> entityClass, Pageable pageable) {
        // 创建请求
        SearchRequest searchRequest = createSearchRequest(
            searchWords,
            Collections.singleton(indexName),
            pageable
        );
        // 请求发送
        SearchResponse searchResponse = doSend(searchRequest);

        List<T> entityList = searchItemBuilder.build(searchResponse.getHits().getHits(), entityClass);

        return new PageImpl<>(
            entityList,
            pageable,
            searchResponse.getHits().getTotalHits().value
        );
    }

    @Override
    public Page compositeSearch(String keyword, SearchType searchType, Pageable pageable) {

        Set<String> searchIndex = searchIndexMap.get(searchType);
        if (searchIndex == null) {
            searchIndex = parsingSearchIndexName(searchType);
        }

        Set<String> searchField = searchFieldMap.get(searchType);
        if (searchField == null) {
            searchField = parsingSearchField(searchType);
        }

        SearchRequest searchRequest = createSearchRequest(searchIndex, searchField, keyword, searchType, pageable);
        return doSearchResponse(searchRequest, pageable);
    }

    /**
     * 发送es请求
     *
     * @param searchRequest
     * @param pageable
     * @return
     */
    private Page doSearchResponse(SearchRequest searchRequest, Pageable pageable) {
        SearchResponse searchResponse = doSend(searchRequest);
        return new PageImpl<>(
            getScoreSortList(searchResponse),
            pageable,
            searchResponse.getHits().getTotalHits().value
        );
    }

    private List<Object> getScoreSortList(SearchResponse search) {
        List<Integer> scoreList = Arrays.stream(search.getHits().getHits())
            .sorted((h1, h2) -> Float.compare(h1.getScore(), h2.getScore()) * -1)
            .map(SearchHit::hashCode)
            .collect(Collectors.toList());

        Map<Integer, Object> resultMap = searchItemBuilder.build(search.getHits().getHits());
        return LambdaUtil.toList(scoreList, integer -> {
            Object key = resultMap.get(integer);
            if (key == null) {
                return null;
            }
            return key;
        });
    }

    private SearchRequest createSearchRequest(Set<String> searchIndex, Set<String> searchField, String keyword, SearchType searchType, Pageable pageable) {
        SearchRequest bookchartSearch = new SearchRequest(searchIndex.toArray(new String[0]));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//        搜索关键词
        boolQuery.must(
            QueryBuilders.multiMatchQuery(keyword, searchField.toArray(new String[0]))
        );
        searchSourceBuilder
            .from(pageable.getPageNumber() * pageable.getPageSize())
            .size(pageable.getPageSize())
            .query(boolQuery);
        bookchartSearch.source(searchSourceBuilder);
        return bookchartSearch;
    }

    private SearchRequest createSearchRequest(Map<String, Map<String, String>> key, Set<String> searchIndex, Pageable pageable) {
        SearchRequest bookchartSearch = new SearchRequest(searchIndex.toArray(new String[0]));
        // 创建请求接口
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 一对多匹配
        Map<String, String> wildcards = key.getOrDefault("wildcards", Collections.emptyMap());
        if (!wildcards.isEmpty()) {
            for (String keys : wildcards.keySet()) {
                // 匹配多个字段
                boolQuery.must(
                    QueryBuilders.multiMatchQuery(wildcards.get(keys), keys.split(","))
                );
            }
        }
        // 一对一匹配
        Map<String, String> match = key.getOrDefault("match", Collections.emptyMap());
        if (!match.isEmpty()) {
            for (String keys : match.keySet()) {
                // 单个匹配
                boolQuery.must(
                    QueryBuilders.matchQuery(keys, match.get(keys))
                );
            }
        }
        // boolean值匹配
        Map<String, String> term = key.getOrDefault("term", Collections.emptyMap());
        if (!term.isEmpty()) {
            for (String keys : term.keySet()) {
                boolQuery.must(
                    QueryBuilders.termQuery(keys, false)
                );
            }
        }
        // 范围筛选
        Map<String, String> range = key.getOrDefault("range", Collections.emptyMap());
        if (!range.isEmpty()) {
            Long startTime = Long.valueOf(range.get("startTime"));
            Long endTime = Long.valueOf(range.get("endTime"));
            boolQuery.must(
                QueryBuilders.rangeQuery("createdAt").gt(startTime)
                    .lt(endTime)
            );
        }
        // 设置排序规则
        if (boolQuery.hasClauses()) {
            if (match.size() == 1 && match.containsKey("institutionId")) {
                searchSourceBuilder.sort("id", SortOrder.DESC);
            } else {
                searchSourceBuilder.sort("_score");
            }
        } else {
            searchSourceBuilder.sort("id", SortOrder.DESC);
        }
        // 排除字段 -- 过滤
        Map<String, String> mustNot = key.getOrDefault("mustNot", Collections.emptyMap());
        if (!mustNot.isEmpty()) {
            for (String keys : mustNot.keySet()) {
                boolQuery.mustNot(
                    QueryBuilders.matchQuery(mustNot.get(keys), keys)
                );
            }
        }
        // 固定字段
        boolQuery.must(
            QueryBuilders.termQuery("isDeleted", false)
        );
        boolQuery.must(
            QueryBuilders.boolQuery()
                .should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("isBlocked")))
                .should(QueryBuilders.matchQuery("isBlocked", false))
                .minimumShouldMatch(1)
        );
        searchSourceBuilder
            .from(pageable.getPageNumber() * pageable.getPageSize())
            .size(pageable.getPageSize())
            .timeout(new TimeValue(10000))
            .query(boolQuery);
        // 请求
        bookchartSearch.source(searchSourceBuilder);
        return bookchartSearch;
    }

    /**
     * 请求接口
     *
     * @param searchRequest 请求
     * @return
     */
    private SearchResponse doSend(SearchRequest searchRequest) {
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            //throw new ServiceException(ServiceErrorCode.C5200.description());
        }
        return searchResponse;
    }

    /**
     * 获取对应的索引
     *
     * @param searchType
     * @return
     */
    private Set<String> parsingSearchIndexName(SearchType searchType) {

        Set<String> searchIndex = new HashSet<>();

        for (Class entityClass : searchType.getSearchEntityArray()) {
            Annotation annotation = entityClass.getDeclaredAnnotation(Document.class);
            if (!(annotation instanceof Document)) {
                continue;
            }
            Document document = (Document) annotation;
            String indexName = document.indexName();
            if (StringUtils.isBlank(indexName)) {
                continue;
            }
            searchIndex.add(indexName);
        }
        searchIndexMap.put(searchType, searchIndex);
        return searchIndex;
    }

    /**
     * 分词搜索
     *
     * @param searchType
     * @return
     */
    private Set<String> parsingSearchField(SearchType searchType) {

        Set<String> searchField = new HashSet<>();
        for (Class entityClass : searchType.getSearchEntityArray()) {
            for (java.lang.reflect.Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                Field analyzerField;
                if ((analyzerField = field.getAnnotation(Field.class)) == null) {
                    continue;
                }
                if ("ik_max_word".equals(analyzerField.analyzer())) {
                    searchField.add(field.getName());
                }
            }
        }
        searchFieldMap.put(searchType, searchField);
        return searchField;
    }

}
