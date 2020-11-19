package com.chenxiao.elasticsearch.utils;


import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @version V1.0
 * @author: csz
 * @Title
 * @Package: com.bookchart.common.util
 * @Description:
 * @date: 2020/05/12
 */
public enum

LambdaUtil {
    ;

    /**
     * 转换列表
     *
     * @param sourceList 源列表
     * @param mapFun     转换时的方法
     * @param nullable   是否可为空
     * @param <R>        源列表类型
     * @param <T>        目标方法类型
     * @return 转换后的列表
     */
    public static <R, T> List<T> toList(Collection<R> sourceList, Function<R, T> mapFun, boolean nullable) {

        if (sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        Stream<T> stream = sourceList.stream()
            .map(mapFun);

        if (!nullable) {
            stream = stream.filter(Objects::nonNull);
        }

        return stream.collect(Collectors.toList());

    }

    /**
     * 转换列表 nullable=false
     *
     * @param sourceList 源列表
     * @param mapFun     转换时的方法
     * @param <R>        源列表类型
     * @param <T>        目标方法类型
     * @return 转换后的列表
     */
    public static <R, T> List<T> toList(Collection<R> sourceList, Function<R, T> mapFun) {
        return toList(sourceList, mapFun, false);
    }

    public static <R> R getFirstObj(Collection<R> sourceList, Predicate<R> mapFun) {
        return sourceList.stream()
            .filter(mapFun)
            .findFirst()
            .orElse(null);
    }

    public static <R, K> R getFirst(Collection<R> sourceList, K key, Function<R, K> mapFun) {
        return getFirstObj(sourceList, source -> Objects.equals(key, mapFun.apply(source)));
    }

    public static <R, K> List<R> getOthers(Collection<R> sourceList, K key, Function<R, K> mapFun) {
        return sourceList.stream()
            .filter(source -> !Objects.equals(key, mapFun.apply(source)))
            .collect(Collectors.toList());
    }


    /**
     * 带中间列表的转换
     *
     * @param sourceList 源列表
     * @param mapFun     转换源对象转换成中间对象时的方法
     * @param convertFun 将中间列表转换成目标列表的方法
     * @param <R>        源列表类型
     * @param <M>        中间列表类型
     * @param <T>        目标列表类型
     * @return 目标列表
     */
    public static <R, M, T> List<T> toList(List<R> sourceList, Function<R, M> mapFun, Function<List<M>, List<T>> convertFun) {

        if (sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        List<M> mediumList = toList(sourceList, mapFun);

        if (mediumList.isEmpty()) {
            return new ArrayList<>();
        }

        return convertFun.apply(mediumList);

    }

    /**
     * 列表转map
     *
     * @param sourceList 源列表
     * @param keyFun     生成键的方法
     * @param <R>        源类型
     * @param <K>        键类型
     * @return map
     */
    public static <R, K> Map<K, R> toMap(List<R> sourceList, Function<R, K> keyFun) {

        if (sourceList.isEmpty()) {
            return new HashMap<>();
        }

        return sourceList.stream()
            .collect(Collectors.toMap(keyFun, Function.identity(), replaceMergeFunction()));


    }

    /**
     * 将List组装成 <对象id -> 对象>
     * @param sourceList
     * @param keyFun
     * @param <R>
     * @param <K>
     * @return
     */
    public static <R, K> Map<K, List<R>> toMapById(List<R> sourceList, Function<R, K> keyFun) {
        if (sourceList.isEmpty()) {
            return Collections.emptyMap();
        }
        return sourceList
            .stream()
            .distinct()
            .filter(Objects::nonNull)
            .collect(
                Collectors.groupingBy(
                    keyFun,
                    Collectors.toList()
                ));
    }

    /**
     * 带列表转换的生成map
     *
     * @param sourceList    源列表
     * @param targetListFun 转换类别的方法
     * @param keyFun        生成键的方法
     * @param <R>           源类型
     * @param <T>           目标类型
     * @param <K>           键类型
     * @return map
     */
    public static <R, T, K> Map<K, T> toMap(List<R> sourceList, Function<List<R>, List<T>> targetListFun, Function<T, K> keyFun) {

        if (sourceList.isEmpty()) {
            return new HashMap<>();
        }

        List<T> targetList = targetListFun.apply(sourceList);

        return toMap(targetList, keyFun);

    }

    /**
     * 列表转map
     *
     * @param sourceList
     * @param keyFun
     * @param <R>
     * @param <K>
     * @return
     */
    public static <R, K> Map<K, List<R>> toMaps(List<R> sourceList, Function<R, K> keyFun) {
        if (sourceList.isEmpty()) {
            return new HashMap<>();
        }
        return sourceList.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                keyFun,
                Collectors.toList()
            ));
    }

    /**
     * 使用 Collectors.toMap 时的重复键策略(保留后者)
     *
     * @param <U> 列表值类型
     * @return replaceMergeFunction
     */
    public static <U> BinaryOperator<U> replaceMergeFunction() {
        return (k1, k2) -> k2;
    }

    /**
     * 获取集合中的最大数值
     *
     * @param sourceList
     * @param numFun
     * @param <T>
     * @return
     */
    public static <T> Long gen(List<T> sourceList, Function<T, Long> numFun) {
        return sourceList.stream()
            .filter(Objects::nonNull)
            .map(numFun)
            .max(Long::compareTo)
            .orElse(0L) + 1L;
    }
}
