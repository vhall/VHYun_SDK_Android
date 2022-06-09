package com.vhall.opensdk.beautysource;

import android.support.annotation.NonNull;

import com.vhall.beautifykit.entity.FaceBeautyBean;
import com.vhall.beautifykit.entity.FaceBeautyFilterBean;
import com.vhall.beautifykit.entity.FaceBeautyStyleBean;
import com.vhall.beautifykit.entity.ModelAttributeData;
import com.vhall.beautifykit.infe.AbstractFaceBeautyDataFactory;
import com.vhall.opensdk.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DESC：美颜业务工厂
 * Created on 2021/3/1
 */
public class FaceBeautyDataFactory extends AbstractFaceBeautyDataFactory {

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

    public FaceBeautyDataFactory(FaceBeautyListener listener) {
    }


    /**
     * 获取美肤参数列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyBean> getSkinBeauty() {
        ArrayList<FaceBeautyBean> params = new ArrayList<>();
        params.add(new FaceBeautyBean(
                        "test",
                        R.string.beauty_box_heavy_blur_fine,
                        R.drawable.icon_beauty_skin_buffing_close_selector,
                        R.drawable.icon_beauty_skin_buffing_open_selector
                )
        );
        return params;
    }

    /**
     * 获取美型参数列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyBean> getShapeBeauty() {
        ArrayList<FaceBeautyBean> params = new ArrayList<>();
        params.add(new FaceBeautyBean(
                        "test",
                        R.string.beauty_box_cheek_thinning,
                        R.drawable.icon_beauty_shape_face_cheekthin_open_selector,
                        R.drawable.icon_beauty_shape_face_cheekthin_close_selector
                )
        );
        params.add(new FaceBeautyBean(
                        "test2",
                        R.string.beauty_box_eye_circle,
                        R.drawable.icon_beauty_shape_face_cheekthin_open_selector,
                        R.drawable.icon_beauty_shape_face_cheekthin_close_selector
                )
        );
        return params;
    }

    /**
     * 获取美型参数列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyBean> getShapeBeautySubItem() {
        return new ArrayList<>();
    }


    /**
     * 获取美肤、美型扩展参数
     *
     * @return
     */
    @NonNull
    @Override
    public HashMap<String, ModelAttributeData> getModelAttributeRange() {
        HashMap<String, ModelAttributeData> params = new HashMap<>();
        /*美肤*/
        params.put("test", new ModelAttributeData(0.3, 0.0, 0.0, 1.0));
        /*美型*/
        params.put("test2", new ModelAttributeData(1.0, 0.0, 0.0, 1.0));
        return params;
    }


    /**
     * 获取滤镜参数列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyFilterBean> getBeautyFilters() {
        return new ArrayList<>();
    }

    /**
     * 获取当前滤镜下标
     *
     * @return
     */
    @Override
    public int getCurrentFilterIndex() {
        return 0;
    }

    /**
     * 设置当前滤镜下标
     *
     * @param currentFilterIndex
     */
    @Override
    public void setCurrentFilterIndex(int currentFilterIndex) {
    }

    /**
     * 获取推荐风格列表
     *
     * @return
     */
    @NonNull
    @Override
    public ArrayList<FaceBeautyStyleBean> getBeautyStyles() {
        return new ArrayList<>();
    }


    /**
     * 获取当前风格推荐标识
     *
     * @return
     */
    @Override
    public int getCurrentStyleIndex() {
        return 0;
    }

    /**
     * 设置风格推荐标识
     *
     * @param styleIndex
     */
    @Override
    public void setCurrentStyleIndex(int styleIndex) {
    }

    /**
     * 美颜开关设置
     *
     * @param enable
     */
    @Override
    public void enableFaceBeauty(boolean enable) {
    }

    /**
     * 获取模型参数
     *
     * @param key 名称标识
     * @return 属性值
     */
    @Override
    public double getParamIntensity(@NonNull String key) {
        return 0;
    }

    /**
     * 设置模型参数
     *
     * @param key   名称标识
     * @param value 属性值
     */
    @Override
    public void updateParamIntensity(@NonNull String key, double value) {
    }

    @Override
    public String getCurrentOneHotFaceShape() {
        return "";
    }

    @Override
    public void setCurrentOneHotFaceShape(String faceShape) {
    }


    /**
     * 设置当前脸型的UI值
     */
    public void setCurrentFaceShapeUIValue(HashMap<String, Double> hashMap) {
    }

    /**
     * 获取当前脸型的UI值
     */
    public HashMap<String, Double> getCurrentFaceShapeUIValue() {
        return new HashMap<>();
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
    }

    /**
     * 更换滤镜强度
     *
     * @param intensity 滤镜强度
     */
    @Override
    public void updateFilterIntensity(double intensity) {
    }

    /**
     * 设置推荐风格
     *
     * @param name
     */
    @Override
    public void onStyleSelected(String name) {
    }
}
