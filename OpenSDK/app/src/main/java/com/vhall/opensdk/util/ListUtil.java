package com.vhall.opensdk.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwp on 2019/3/16
 */
public class ListUtil {
    /**
     * 将一个list均分成n个list,主要通过偏移量来实现的
     *
     * @param source
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int remaider = source.size() % n;  //(先计算出余数)
        int number = source.size() / n;  //然后是商
        int offset = 0;//偏移量
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    /**
     * 将一个list 按n进行拆分
     *
     * @param source
     * @param n
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> sublistAsNum(List<T> source, int n) {
        List<List<T>> result = new ArrayList<>();
        int remainder = source.size() % n;//计算剩余
        int number = source.size() / n;
        if (remainder > 0) {
            number = number + 1;
        }
        for (int i = 0; i < number; i++) {
            List<T> vaule = null;
            if (i != number - 1) {
                vaule = source.subList(i * n, (i + 1) * n);
            } else {
                vaule = source.subList(i * n, source.size());
            }
            result.add(vaule);
        }
        return result;
    }
}
