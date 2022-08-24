package com.vhall.opensdk.beautysource;

import androidx.annotation.NonNull;

import com.faceunity.core.controller.facebeauty.FaceBeautyParam;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.model.facebeauty.FaceBeauty;
import com.vhall.beautify.DefaultFaceBeautyDataFactory;
import com.vhall.beautify.VHBeautifyKit;
import com.vhall.beautifykit.entity.FaceBeautyBean;
import com.vhall.beautifykit.entity.FaceBeautyFilterBean;
import com.vhall.beautifykit.entity.FaceBeautyStyleBean;
import com.vhall.beautifykit.entity.ModelAttributeData;
import com.vhall.beautifykit.infe.AbstractFaceBeautyDataFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DESC：美颜业务工厂
 * Created on 2021/3/1
 */
public class FaceBeautyDataFactory extends AbstractFaceBeautyDataFactory {

    private final String TAG = "FaceBeautyDataFactory";

    public interface FaceBeautyListener {
        /**
         * 风格切换
         *
         * @param res
         */
        void onFilterSelected(int res);

        /**
         * 美颜开关
         *
         * @param enable
         */
        void onFaceBeautyEnable(boolean enable);
    }

    interface FaceBeautySetParamInterface {
        /**
         * 设置属性值
         *
         * @param value
         */
        void setValue(double value);
    }

    interface FaceBeautyGetParamInterface {
        /**
         * 获取属性值
         *
         * @return
         */
        double getValue();
    }

    /*推荐风格标识*/
    private static int currentStyleIndex = -1;
    /*默认滤镜选中下标*/
    private int currentFilterIndex = 0;
    /*业务回调*/
    private final FaceBeautyListener mFaceBeautyListener;


    public FaceBeautyDataFactory(FaceBeautyListener listener) {
        mFaceBeautyListener = listener;
    }


    /**
     * 获取美肤参数列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyBean> getSkinBeauty() {
        return FaceBeautySource.buildSkinParams();
    }

    /**
     * 获取美型参数列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyBean> getShapeBeauty() {
        return FaceBeautySource.buildShapeParams();
    }

    /**
     * 获取美型参数列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyBean> getShapeBeautySubItem() {
        return FaceBeautySource.buildFaceShapeSubItemParams();
    }


    /**
     * 获取美肤、美型扩展参数
     *
     * @return
     */
    @NonNull
    @Override
    public HashMap<String, ModelAttributeData> getModelAttributeRange() {
        return FaceBeautySource.buildModelAttributeRange();
    }


    /**
     * 获取滤镜参数列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyFilterBean> getBeautyFilters() {
        ArrayList<FaceBeautyFilterBean> filterBeans = FaceBeautySource.buildFilters();
        for (int i = 0; i < filterBeans.size(); i++) {
            if (filterBeans.get(i).getKey().equals(currentFaceBeauty().getFilterName())) {
                filterBeans.get(i).setIntensity(currentFaceBeauty().getFilterIntensity());
                currentFilterIndex = i;
            }

        }
        return filterBeans;
    }

    /**
     * 获取当前滤镜下标
     *
     * @return
     */
    @Override
    public int getCurrentFilterIndex() {
        return currentFilterIndex;
    }

    /**
     * 设置当前滤镜下标
     *
     * @param currentFilterIndex
     */
    @Override
    public void setCurrentFilterIndex(int currentFilterIndex) {
        this.currentFilterIndex = currentFilterIndex;
    }

    /**
     * 获取推荐风格列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyStyleBean> getBeautyStyles() {
        return FaceBeautySource.buildStylesParams();
    }


    /**
     * 获取当前风格推荐标识
     *
     * @return
     */
    @Override
    public int getCurrentStyleIndex() {
        return currentStyleIndex;
    }

    /**
     * 设置风格推荐标识
     *
     * @param styleIndex
     */
    @Override
    public void setCurrentStyleIndex(int styleIndex) {
        currentStyleIndex = styleIndex;
    }

    /**
     * 美颜开关设置
     *
     * @param enable
     */
    @Override
    public void enableFaceBeauty(boolean enable) {
        mFaceBeautyListener.onFaceBeautyEnable(enable);
    }

    /**
     * 获取模型参数
     *
     * @param key 名称标识
     * @return 属性值
     */
    @Override
    public double getParamIntensity(@NonNull String key) {
        return VHBeautifyKit.getInstance().getParamIntensity(key);
    }

    /**
     * 设置模型参数
     *
     * @param key   名称标识
     * @param value 属性值
     */
    @Override
    public void updateParamIntensity(@NonNull String key, double value) {
        VHBeautifyKit.getInstance().updateParamIntensity(key, value);
    }

    @Override
    public String getCurrentOneHotFaceShape() {
        return CurrentFaceShapeUIValue.currentFaceShape == null ? FaceBeautyParam.CHEEK_V_INTENSITY : CurrentFaceShapeUIValue.currentFaceShape;
    }

    @Override
    public void setCurrentOneHotFaceShape(String faceShape) {
        CurrentFaceShapeUIValue.currentFaceShape = faceShape;
    }


    /**
     * 设置当前脸型的UI值
     */
    public void setCurrentFaceShapeUIValue(HashMap<String, Double> hashMap) {
        CurrentFaceShapeUIValue.currentFaceShapeValue.clear();
        CurrentFaceShapeUIValue.currentFaceShapeValue.putAll(hashMap);
    }

    /**
     * 获取当前脸型的UI值
     */
    public HashMap<String, Double> getCurrentFaceShapeUIValue() {
        return CurrentFaceShapeUIValue.currentFaceShapeValue;
    }

    /**
     * 切换滤镜
     *
     * @param name      滤镜名称标识
     * @param intensity 滤镜强度
     * @param resID     滤镜名称
     */
    @Override
    public void onFilterSelected(@NonNull String name, double intensity, int resID) {
        VHBeautifyKit.getInstance().setFilter(name, intensity);
        mFaceBeautyListener.onFilterSelected(resID);
    }

    /**
     * 更换滤镜强度
     *
     * @param intensity 滤镜强度
     */
    @Override
    public void updateFilterIntensity(double intensity) {
        VHBeautifyKit.getInstance().setFilterIntensity(intensity);
    }

    /**
     * 设置推荐风格
     *
     * @param name
     */
    @Override
    public void onStyleSelected(String name) {
        if (name == null) {
            DefaultFaceBeautyDataFactory.currentBeauty = DefaultFaceBeautyDataFactory.defaultFaceBeauty;
            FURenderKit.getInstance().setFaceBeauty(DefaultFaceBeautyDataFactory.currentBeauty);
        } else {
            Runnable runnable = FaceBeautySource.styleParams.get(name);
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    private FaceBeauty currentFaceBeauty() {
        return DefaultFaceBeautyDataFactory.currentBeauty;
    }

    /**
     * 用于记录当前脸型的UI值 -> 用于用户下次点入的时候恢复
     */
    static class CurrentFaceShapeUIValue {
        /* 当前生效的脸型 */
        public static String currentFaceShape = FaceBeautyParam.CHEEK_V_INTENSITY;
        /* 当前脸型的UI值 */
        public static HashMap<String, Double> currentFaceShapeValue = new HashMap<>();
    }
}
