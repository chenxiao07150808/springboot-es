package com.chenxiao.elasticsearch.service;


import com.chenxiao.elasticsearch.enums.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @name:
 * @Author: cx
 * @date: 2020/11/19
 * @describe: es操作
 */
public interface SearchService {
    /**
     *
     * @param searchWords 搜索字段与搜索值
     * @param entityClass
     * @param indexName 对应的文档 - 数据库表
     * @param pageable 页码
     * @param <T>
     * @return
     */
    <T> Page<T> search(Map<String, Map<String, String>> searchWords, String indexName, Class<T> entityClass, Pageable pageable);


    /**
     * 综合搜索
     * @param keyword   关键值
     * @param searchType  字段 -- 通过自定义标签注解 获取字段 进行多字段匹配
     * @param pageable 页数
     * @return 混合实体
     *
     */
    Page compositeSearch(String keyword, SearchType searchType, Pageable pageable);
}
