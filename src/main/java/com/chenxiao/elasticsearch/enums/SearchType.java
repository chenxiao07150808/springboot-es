package com.chenxiao.elasticsearch.enums;

import com.chenxiao.elasticsearch.entity.Book;

/**
 * @name:
 * @Author: cx
 * @date: 2020/11/19
 * @describe:
 */
public enum SearchType {
    BOOK(Book .class),

        ;

    private final Class[] searchEntityArray;

    SearchType(Class...searchEntity) {
        this.searchEntityArray = searchEntity;
    }

    public Class[] getSearchEntityArray() {
        return searchEntityArray;
    }
}
