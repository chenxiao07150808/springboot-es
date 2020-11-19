package com.chenxiao.elasticsearch.controller;

import com.chenxiao.elasticsearch.entity.Book;
import com.chenxiao.elasticsearch.service.SearchService;
import com.chenxiao.elasticsearch.utils.PageParamsForm;
import com.chenxiao.elasticsearch.utils.PaginationUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @name:
 * @Author: cx
 * @date: 2020/11/19
 * @describe:
 */
@Api(value = "书籍模块", tags = "es书籍模块")
@RestController
@RequestMapping("/book")
public class MyController {

    @Autowired
    private SearchService searchService;


    @GetMapping
    @ApiOperation(value = "查询书籍")
    public Page<Book> searchBook(@RequestParam String name, @RequestParam Integer pageSize, @RequestParam Integer pageNum) {
        Map<String, String> map1 = new HashMap<>(1);
        map1.put("name", name);
        HashMap<String, Map<String, String>> allMap = new HashMap<>(1);
        allMap.put("wildcards", map1);
        return searchService.search(allMap,"dubang_book", Book.class, PaginationUtils.of(new PageParamsForm(pageNum, pageSize)));
    }

}
