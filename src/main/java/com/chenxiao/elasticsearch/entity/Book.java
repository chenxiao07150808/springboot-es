package com.chenxiao.elasticsearch.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 书籍对象 book
 *
 * @author chenxiao
 * @date 2020-05-11
 */
@Data
public class Book {

    /**
     * id
     */
    private Integer id;

    /**
     * 书籍id
     */
    private String bid;

    private Integer otmId;

    /**
     * 封面
     */
    private String cover;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;
    /**
     * 机构内分类id
     */
    @ApiModelProperty("机构内分类id")
    private Long institutionCategoryId;

    /**
     * 父级分类
     */
    private Long parentCategoryId;
    /**
     * 书籍名称
     */
    private String name;

    private String nameTran;

    /**
     * ISBN码
     */
    private String isbn;

    /**
     * 所属机构id
     */
    private Integer institutionId;

    /**
     * 出版社
     */
    private String publisherName;

    /**
     * 出版时间
     */
    private Date publishTime;

    /**
     * 作者id（目前不用）
     */
    private Integer authorId;

    /**
     * 作者
     */
    private String authorName;

    /**
     * 作者介绍
     */
    private String authorIntroduction;

    /**
     * 书本简介
     */
    private String summary;

    /**
     * 书本介绍
     */
    private String introduction;

    /**
     * 目录
     */
    private String contents;

    /**
     * 零售价
     */
    private Integer retailPrice;

    /**
     * 评分（冗余）
     */
    private Long score;

    /**
     * 想读人数（冗余）
     */
    private Long countToRead;

    /**
     * 在读人数（冗余）
     */
    private Long countReading;

    /**
     * 已读人数（冗余）
     */
    private Long countHadRead;

    /**
     * 其他字段
     */
    private String extra;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;

    /**
     * 是否删除
     */
    private Boolean isDeleted;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 收藏量
     */
    private Long collectCount;

    /**
     * 分享量
     */
    private Long shareCount;

    /**
     * 星级
     */
    private String scoreLevel;
    /**
     * 机构名称
     */
    private String institutionName;


}