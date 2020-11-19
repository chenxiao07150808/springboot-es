package com.chenxiao.elasticsearch.utils;

import io.swagger.annotations.ApiModelProperty;

/**
 * @version V1.0
 * @author: csz
 * @Title
 * @Package: com.bookchart.common.pojo.vo
 * @Description:
 * @date: 2020/02/05
 */
public class PageParamsForm {

    @ApiModelProperty(value = "页数",example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页大小", example = "20")
    private Integer size;

    public PageParamsForm() {
        page = 1;
        size = 20;
    }
    public PageParamsForm(Integer page, Integer size) {
       this.page = page;
       this.size = size;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size == null ? 20 : size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
