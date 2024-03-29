package com.vhall.opensdk.beautysource;

import com.faceunity.core.entity.FUBundleData;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.model.facebeauty.FaceBeauty;
import com.faceunity.core.model.facebeauty.FaceBeautyFilterEnum;
import com.vhall.beautify.DefaultFaceBeautyDataFactory;
import com.vhall.beautify.DemoConfig;
import com.vhall.beautify.type.VHBeautifyFilterConfig;
import com.vhall.beautify.type.VHBeautifyParamConfig;
import com.vhall.beautifykit.entity.FaceBeautyBean;
import com.vhall.beautifykit.entity.FaceBeautyFilterBean;
import com.vhall.beautifykit.entity.FaceBeautyStyleBean;
import com.vhall.beautifykit.entity.ModelAttributeData;
import com.vhall.opensdk.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DESC：美颜数据构造
 * Created on 2021/3/27
 */
public class FaceBeautySource {

    /**
     * 初始化美肤参数
     *
     * @return ArrayList<FaceBeautyBean>
     */
    public static ArrayList<FaceBeautyBean> buildSkinParams() {
        ArrayList<FaceBeautyBean> params = new ArrayList<>();
        params.add(new FaceBeautyBean(
                        VHBeautifyParamConfig.BLUR_INTENSITY, R.string.beauty_box_heavy_blur_fine,
                        R.drawable.icon_beauty_skin_buffing_close_selector, R.drawable.icon_beauty_skin_buffing_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.COLOR_INTENSITY, R.string.beauty_box_color_level,
                        R.drawable.icon_beauty_skin_color_close_selector, R.drawable.icon_beauty_skin_color_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.RED_INTENSITY, R.string.beauty_box_red_level,
                        R.drawable.icon_beauty_skin_red_close_selector, R.drawable.icon_beauty_skin_red_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.SHARPEN_INTENSITY, R.string.beauty_box_sharpen,
                        R.drawable.icon_beauty_skin_sharpen_close_selector, R.drawable.icon_beauty_skin_sharpen_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_BRIGHT_INTENSITY, R.string.beauty_box_eye_bright,
                        R.drawable.icon_beauty_skin_eyes_bright_close_selector, R.drawable.icon_beauty_skin_eyes_bright_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.TOOTH_WHITEN_INTENSITY, R.string.beauty_box_tooth_whiten,
                        R.drawable.icon_beauty_skin_teeth_close_selector, R.drawable.icon_beauty_skin_teeth_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.REMOVE_POUCH_INTENSITY, R.string.beauty_micro_pouch,
                        R.drawable.icon_beauty_skin_dark_circles_close_selector, R.drawable.icon_beauty_skin_dark_circles_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.REMOVE_NASOLABIAL_FOLDS_INTENSITY, R.string.beauty_micro_nasolabial,
                        R.drawable.icon_beauty_skin_wrinkle_close_selector, R.drawable.icon_beauty_skin_wrinkle_open_selector
                )
        );
        return params;
    }

    /**
     * 初始化美型参数
     *
     * @return ArrayList<FaceBeautyBean>
     */
    public static ArrayList<FaceBeautyBean> buildShapeParams() {
        ArrayList<FaceBeautyBean> params = new ArrayList<>();
//        params.add(
//                new FaceBeautyBean(
//                        "", R.string.avatar_face_face,
//                        R.drawable.icon_beauty_shape_face_shape_close_selector, R.drawable.icon_beauty_shape_face_shape_open_selector, FaceBeautyBean.ButtonType.SUB_ITEM_BUTTON
//                )
//        );

        //瘦脸
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_THINNING_INTENSITY, R.string.beauty_box_cheek_thinning,
                        R.drawable.icon_beauty_shape_face_cheekthin_close_selector, R.drawable.icon_beauty_shape_face_cheekthin_open_selector
                )
        );

        //V脸
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_V_INTENSITY, R.string.beauty_box_cheek_v,
                        R.drawable.icon_beauty_shape_face_v_close_selector, R.drawable.icon_beauty_shape_face_v_open_selector
                )
        );

        //窄脸
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_NARROW_INTENSITY, R.string.beauty_box_cheek_narrow,
                        R.drawable.icon_beauty_shape_face_narrow_close_selector, R.drawable.icon_beauty_shape_face_narrow_open_selector
                )
        );

        //小脸 -> 短脸  --使用的参数是以前小脸的
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_SHORT_INTENSITY, R.string.beauty_box_cheek_short,
                        R.drawable.icon_beauty_shape_face_short_close_selector, R.drawable.icon_beauty_shape_face_short_open_selector
                )
        );

        //小脸 -> 新增
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_SMALL_INTENSITY_V2, R.string.beauty_box_cheek_small,
                        R.drawable.icon_beauty_shape_face_little_close_selector, R.drawable.icon_beauty_shape_face_little_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.INTENSITY_CHEEKBONES_INTENSITY, R.string.beauty_box_cheekbones,
                        R.drawable.icon_beauty_shape_cheek_bones_close_selector, R.drawable.icon_beauty_shape_cheek_bones_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.INTENSITY_LOW_JAW_INTENSITY, R.string.beauty_box_lower_jaw,
                        R.drawable.icon_beauty_shape_lower_jaw_close_selector, R.drawable.icon_beauty_shape_lower_jaw_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_ENLARGING_INTENSITY, R.string.beauty_box_eye_enlarge,
                        R.drawable.icon_beauty_shape_enlarge_eye_close_selector, R.drawable.icon_beauty_shape_enlarge_eye_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_CIRCLE_INTENSITY, R.string.beauty_box_eye_circle,
                        R.drawable.icon_beauty_shape_round_eye_close_selector, R.drawable.icon_beauty_shape_round_eye_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHIN_INTENSITY, R.string.beauty_box_intensity_chin,
                        R.drawable.icon_beauty_shape_chin_close_selector, R.drawable.icon_beauty_shape_chin_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.FOREHEAD_INTENSITY, R.string.beauty_box_intensity_forehead,
                        R.drawable.icon_beauty_shape_forehead_close_selector, R.drawable.icon_beauty_shape_forehead_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.NOSE_INTENSITY, R.string.beauty_box_intensity_nose,
                        R.drawable.icon_beauty_shape_thin_nose_close_selector, R.drawable.icon_beauty_shape_thin_nose_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.MOUTH_INTENSITY, R.string.beauty_box_intensity_mouth,
                        R.drawable.icon_beauty_shape_mouth_close_selector, R.drawable.icon_beauty_shape_mouth_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CANTHUS_INTENSITY, R.string.beauty_micro_canthus,
                        R.drawable.icon_beauty_shape_open_eyes_close_selector, R.drawable.icon_beauty_shape_open_eyes_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_SPACE_INTENSITY, R.string.beauty_micro_eye_space,
                        R.drawable.icon_beauty_shape_distance_close_selector, R.drawable.icon_beauty_shape_distance_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_ROTATE_INTENSITY, R.string.beauty_micro_eye_rotate,
                        R.drawable.icon_beauty_shape_angle_close_selector, R.drawable.icon_beauty_shape_angle_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.LONG_NOSE_INTENSITY, R.string.beauty_micro_long_nose,
                        R.drawable.icon_beauty_shape_proboscis_close_selector, R.drawable.icon_beauty_shape_proboscis_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.PHILTRUM_INTENSITY, R.string.beauty_micro_philtrum,
                        R.drawable.icon_beauty_shape_shrinking_close_selector, R.drawable.icon_beauty_shape_shrinking_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.SMILE_INTENSITY, R.string.beauty_micro_smile,
                        R.drawable.icon_beauty_shape_smile_close_selector, R.drawable.icon_beauty_shape_smile_open_selector
                )
        );
        return params;
    }

    /**
     * 加载脸型子项
     *
     * @return
     */
    public static ArrayList<FaceBeautyBean> buildFaceShapeSubItemParams() {
        return buildSubItemParams(VHBeautifyParamConfig.FACE_SHAPE);
    }

    public static ArrayList<FaceBeautyBean> buildSubItemParams(String key) {
        ArrayList<FaceBeautyBean> params = new ArrayList<>();
//        if (key != null && !key.isEmpty()) {
//            if (key.equals(FaceBeautyParam.FACE_SHAPE)) {
//                //返回
//                params.add(
//                        new FaceBeautyBean(
//                                "", R.string.back,
//                                R.mipmap.icon_beauty_back, R.mipmap.icon_beauty_back, FaceBeautyBean.ButtonType.BACK_BUTTON
//                        )
//                );
//
//                //自然 V脸 -> 自然脸
//                params.add(
//                        new FaceBeautyBean(
//                                FaceBeautyParam.CHEEK_V_INTENSITY, R.string.beauty_box_cheek_natural,
//                                R.drawable.icon_beauty_shape_face_natural_close_selector, R.drawable.icon_beauty_shape_face_natural_open_selector
//                        )
//                );
//
//                //女神 瘦脸 -> 女神脸
//                params.add(
//                        new FaceBeautyBean(
//                                FaceBeautyParam.CHEEK_THINNING_INTENSITY, R.string.beauty_box_cheek_goddess,
//                                R.drawable.icon_beauty_shape_face_goddess_close_selector, R.drawable.icon_beauty_shape_face_goddess_open_selector
//                        )
//                );
//
//                //长脸
//                params.add(
//                        new FaceBeautyBean(
//                                FaceBeautyParam.CHEEK_LONG_INTENSITY, R.string.beauty_box_cheek_long_face,
//                                R.drawable.icon_beauty_shape_face_long_close_selector, R.drawable.icon_beauty_shape_face_long_open_selector
//                        )
//                );
//
//                //圆脸
//                params.add(
//                        new FaceBeautyBean(
//                                FaceBeautyParam.CHEEK_CIRCLE_INTENSITY, R.string.beauty_box_cheek_round_face,
//                                R.drawable.icon_beauty_shape_face_round_close_selector, R.drawable.icon_beauty_shape_face_round_open_selector
//                        )
//                );
//            }
//        }

        return params;
    }

    /**
     * 初始化参数扩展列表
     *
     * @return HashMap<String, ModelAttributeData>
     */
    public static HashMap<String, ModelAttributeData> buildModelAttributeRange() {
        HashMap<String, ModelAttributeData> params = new HashMap<>();
        /*美肤*/
        params.put(VHBeautifyParamConfig.COLOR_INTENSITY, new ModelAttributeData(0.3, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.BLUR_INTENSITY, new ModelAttributeData(4.2, 0.0, 0.0, 6.0));
        params.put(VHBeautifyParamConfig.RED_INTENSITY, new ModelAttributeData(0.3, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.SHARPEN_INTENSITY, new ModelAttributeData(0.2, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.EYE_BRIGHT_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.TOOTH_WHITEN_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.REMOVE_POUCH_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.REMOVE_NASOLABIAL_FOLDS_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        /*美型*/
        params.put(VHBeautifyParamConfig.FACE_SHAPE_INTENSITY, new ModelAttributeData(1.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_THINNING_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_LONG_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_CIRCLE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_V_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_NARROW_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_SHORT_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_SMALL_INTENSITY_V2, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.INTENSITY_CHEEKBONES_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.INTENSITY_LOW_JAW_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.EYE_ENLARGING_INTENSITY, new ModelAttributeData(0.4, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.EYE_CIRCLE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHIN_INTENSITY, new ModelAttributeData(0.3, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.FOREHEAD_INTENSITY, new ModelAttributeData(0.3, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.NOSE_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.MOUTH_INTENSITY, new ModelAttributeData(0.4, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CANTHUS_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.EYE_SPACE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.EYE_ROTATE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.LONG_NOSE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.PHILTRUM_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.SMILE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        return params;
    }


    /**
     * 初始化滤镜参数
     *
     * @return ArrayList<FaceBeautyFilterBean>
     */
    public static ArrayList<FaceBeautyFilterBean> buildFilters() {
        ArrayList<FaceBeautyFilterBean> filters = new ArrayList<>();
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ORIGIN, R.mipmap.icon_beauty_filter_cancel, R.string.origin, 0.0));
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.ZIRAN, R.mipmap.icon_beauty_filter_natural_1, R.string.ziran_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_2, R.mipmap.icon_beauty_filter_natural_2, R.string.ziran_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_3, R.mipmap.icon_beauty_filter_natural_3, R.string.ziran_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_4, R.mipmap.icon_beauty_filter_natural_4, R.string.ziran_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_5, R.mipmap.icon_beauty_filter_natural_5, R.string.ziran_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_6, R.mipmap.icon_beauty_filter_natural_6, R.string.ziran_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_7, R.mipmap.icon_beauty_filter_natural_7, R.string.ziran_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_8, R.mipmap.icon_beauty_filter_natural_8, R.string.ziran_8));

        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.ZHIGANHUI, R.mipmap.icon_beauty_filter_texture_gray_1, R.string.zhiganhui_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_2, R.mipmap.icon_beauty_filter_texture_gray_2, R.string.zhiganhui_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_3, R.mipmap.icon_beauty_filter_texture_gray_3, R.string.zhiganhui_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_4, R.mipmap.icon_beauty_filter_texture_gray_4, R.string.zhiganhui_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_5, R.mipmap.icon_beauty_filter_texture_gray_5, R.string.zhiganhui_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_6, R.mipmap.icon_beauty_filter_texture_gray_6, R.string.zhiganhui_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_7, R.mipmap.icon_beauty_filter_texture_gray_7, R.string.zhiganhui_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_8, R.mipmap.icon_beauty_filter_texture_gray_8, R.string.zhiganhui_8));

        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.MITAO, R.mipmap.icon_beauty_filter_peach_1, R.string.mitao_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_2, R.mipmap.icon_beauty_filter_peach_2, R.string.mitao_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_3, R.mipmap.icon_beauty_filter_peach_3, R.string.mitao_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_4, R.mipmap.icon_beauty_filter_peach_4, R.string.mitao_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_5, R.mipmap.icon_beauty_filter_peach_5, R.string.mitao_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_6, R.mipmap.icon_beauty_filter_peach_6, R.string.mitao_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_7, R.mipmap.icon_beauty_filter_peach_7, R.string.mitao_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_8, R.mipmap.icon_beauty_filter_peach_8, R.string.mitao_8));

        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.BAILIANG, R.mipmap.icon_beauty_filter_bailiang_1, R.string.bailiang_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_2, R.mipmap.icon_beauty_filter_bailiang_2, R.string.bailiang_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_3, R.mipmap.icon_beauty_filter_bailiang_3, R.string.bailiang_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_4, R.mipmap.icon_beauty_filter_bailiang_4, R.string.bailiang_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_5, R.mipmap.icon_beauty_filter_bailiang_5, R.string.bailiang_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_6, R.mipmap.icon_beauty_filter_bailiang_6, R.string.bailiang_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_7, R.mipmap.icon_beauty_filter_bailiang_7, R.string.bailiang_7));

        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.FENNEN, R.mipmap.icon_beauty_filter_fennen_1, R.string.fennen_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_2, R.mipmap.icon_beauty_filter_fennen_2, R.string.fennen_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_3, R.mipmap.icon_beauty_filter_fennen_3, R.string.fennen_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_5, R.mipmap.icon_beauty_filter_fennen_5, R.string.fennen_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_6, R.mipmap.icon_beauty_filter_fennen_6, R.string.fennen_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_7, R.mipmap.icon_beauty_filter_fennen_7, R.string.fennen_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_8, R.mipmap.icon_beauty_filter_fennen_8, R.string.fennen_8));

        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.LENGSEDIAO, R.mipmap.icon_beauty_filter_lengsediao_1, R.string.lengsediao_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_2, R.mipmap.icon_beauty_filter_lengsediao_2, R.string.lengsediao_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_3, R.mipmap.icon_beauty_filter_lengsediao_3, R.string.lengsediao_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_4, R.mipmap.icon_beauty_filter_lengsediao_4, R.string.lengsediao_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_7, R.mipmap.icon_beauty_filter_lengsediao_7, R.string.lengsediao_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_8, R.mipmap.icon_beauty_filter_lengsediao_8, R.string.lengsediao_8));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_11, R.mipmap.icon_beauty_filter_lengsediao_11, R.string.lengsediao_11));

        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.NUANSEDIAO, R.mipmap.icon_beauty_filter_nuansediao_1, R.string.nuansediao_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.NUANSEDIAO_2, R.mipmap.icon_beauty_filter_nuansediao_2, R.string.nuansediao_2));

        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.GEXING, R.mipmap.icon_beauty_filter_gexing_1, R.string.gexing_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_2, R.mipmap.icon_beauty_filter_gexing_2, R.string.gexing_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_3, R.mipmap.icon_beauty_filter_gexing_3, R.string.gexing_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_4, R.mipmap.icon_beauty_filter_gexing_4, R.string.gexing_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_5, R.mipmap.icon_beauty_filter_gexing_5, R.string.gexing_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_7, R.mipmap.icon_beauty_filter_gexing_7, R.string.gexing_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_10, R.mipmap.icon_beauty_filter_gexing_10, R.string.gexing_10));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_11, R.mipmap.icon_beauty_filter_gexing_11, R.string.gexing_11));

        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.XIAOQINGXIN, R.mipmap.icon_beauty_filter_xiaoqingxin_1, R.string.xiaoqingxin_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.XIAOQINGXIN_3, R.mipmap.icon_beauty_filter_xiaoqingxin_3, R.string.xiaoqingxin_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.XIAOQINGXIN_4, R.mipmap.icon_beauty_filter_xiaoqingxin_4, R.string.xiaoqingxin_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.XIAOQINGXIN_6, R.mipmap.icon_beauty_filter_xiaoqingxin_6, R.string.xiaoqingxin_6));

        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.HEIBAI, R.mipmap.icon_beauty_filter_heibai_1, R.string.heibai_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.HEIBAI_2, R.mipmap.icon_beauty_filter_heibai_2, R.string.heibai_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.HEIBAI_3, R.mipmap.icon_beauty_filter_heibai_3, R.string.heibai_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.HEIBAI_4, R.mipmap.icon_beauty_filter_heibai_4, R.string.heibai_4));

        return filters;
    }

    private static final String CONFIG_BIAOZHUN = "biaozhun";
    private static final String CONFIG_HUAJIAO = "huajiao";
    private static final String CONFIG_KUAISHOU = "kuaishou";
    private static final String CONFIG_QINGYAN = "qingyan";
    private static final String CONFIG_SHANGTANG = "shangtang";
    private static final String CONFIG_YINGKE = "yingke";
    private static final String CONFIG_ZIJIETIAODONG = "zijietiaodong";


    /**
     * 初始化风格推荐
     *
     * @return ArrayList<FaceBeautyBean>
     */
    public static ArrayList<FaceBeautyStyleBean> buildStylesParams() {
        ArrayList<FaceBeautyStyleBean> params = new ArrayList<>();
        params.add(new FaceBeautyStyleBean(CONFIG_KUAISHOU, R.drawable.icon_beauty_style_1_selector, R.string.beauty_face_style_1));
        params.add(new FaceBeautyStyleBean(CONFIG_QINGYAN, R.drawable.icon_beauty_style_2_selector, R.string.beauty_face_style_2));
        params.add(new FaceBeautyStyleBean(CONFIG_ZIJIETIAODONG, R.drawable.icon_beauty_style_3_selector, R.string.beauty_face_style_3));
        params.add(new FaceBeautyStyleBean(CONFIG_HUAJIAO, R.drawable.icon_beauty_style_4_selector, R.string.beauty_face_style_4));
        params.add(new FaceBeautyStyleBean(CONFIG_YINGKE, R.drawable.icon_beauty_style_5_selector, R.string.beauty_face_style_5));
        params.add(new FaceBeautyStyleBean(CONFIG_SHANGTANG, R.drawable.icon_beauty_style_6_selector, R.string.beauty_face_style_6));
        params.add(new FaceBeautyStyleBean(CONFIG_BIAOZHUN, R.drawable.icon_beauty_style_7_selector, R.string.beauty_face_style_7));
        return params;
    }

    /**
     * 风格对应参数配置
     */
    public static HashMap<String, Runnable> styleParams = new HashMap<String, Runnable>() {
        {
            put(CONFIG_KUAISHOU, () -> {
                FaceBeauty model = new FaceBeauty(new FUBundleData(DemoConfig.BUNDLE_FACE_BEAUTIFICATION));
                model.setFaceShapeIntensity(1.0);
                model.setColorIntensity(0.5);
                model.setBlurIntensity(3.6);
                model.setEyeBrightIntensity(0.35);
                model.setToothIntensity(0.25);
                model.setCheekThinningIntensity(0.45);
                model.setCheekVIntensity(0.08);
                model.setCheekSmallIntensityV2(0.05);
                model.setEyeEnlargingIntensityV2(0.3);
                DefaultFaceBeautyDataFactory.currentBeauty = model;
                FURenderKit.getInstance().setFaceBeauty(DefaultFaceBeautyDataFactory.currentBeauty);

            });
            put(CONFIG_QINGYAN, () -> {
                FaceBeauty model = new FaceBeauty(new FUBundleData(DemoConfig.BUNDLE_FACE_BEAUTIFICATION));
                model.setFaceShapeIntensity(1.0);
                model.setFilterName(FaceBeautyFilterEnum.ZIRAN_3);
                model.setFilterIntensity(0.3);
                model.setColorIntensity(0.4);
                model.setRedIntensity(0.2);
                model.setBlurIntensity(3.6);
                model.setEyeBrightIntensity(0.5);
                model.setToothIntensity(0.4);
                model.setCheekThinningIntensity(0.3);
                model.setNoseIntensityV2(0.5);
                model.setEyeEnlargingIntensityV2(0.25);
                DefaultFaceBeautyDataFactory.currentBeauty = model;
                FURenderKit.getInstance().setFaceBeauty(DefaultFaceBeautyDataFactory.currentBeauty);
            });
            put(CONFIG_ZIJIETIAODONG, () -> {
                FaceBeauty model = new FaceBeauty(new FUBundleData(DemoConfig.BUNDLE_FACE_BEAUTIFICATION));
                model.setFaceShapeIntensity(1.0);
                model.setColorIntensity(0.4);
                model.setRedIntensity(0.3);
                model.setBlurIntensity(2.4);
                model.setCheekThinningIntensity(0.3);
                model.setCheekSmallIntensityV2(0.15);
                model.setEyeEnlargingIntensityV2(0.65);
                model.setNoseIntensityV2(0.3);
                DefaultFaceBeautyDataFactory.currentBeauty = model;
                FURenderKit.getInstance().setFaceBeauty(DefaultFaceBeautyDataFactory.currentBeauty);
            });
            put(CONFIG_HUAJIAO, () -> {
                FaceBeauty model = new FaceBeauty(new FUBundleData(DemoConfig.BUNDLE_FACE_BEAUTIFICATION));
                model.setFaceShapeIntensity(1.0);
                model.setColorIntensity(0.7);
                model.setBlurIntensity(3.9);
                model.setCheekThinningIntensity(0.3);
                model.setCheekSmallIntensityV2(0.05);
                model.setEyeEnlargingIntensityV2(0.65);
                DefaultFaceBeautyDataFactory.currentBeauty = model;
                FURenderKit.getInstance().setFaceBeauty(DefaultFaceBeautyDataFactory.currentBeauty);
            });
            put(CONFIG_YINGKE, () -> {
                FaceBeauty model = new FaceBeauty(new FUBundleData(DemoConfig.BUNDLE_FACE_BEAUTIFICATION));
                model.setFaceShapeIntensity(1.0);
                model.setFilterName(FaceBeautyFilterEnum.FENNEN_2);
                model.setFilterIntensity(0.5);
                model.setColorIntensity(0.6);
                model.setBlurIntensity(3.0);
                model.setCheekThinningIntensity(0.5);
                model.setEyeEnlargingIntensityV2(0.65);
                DefaultFaceBeautyDataFactory.currentBeauty = model;
                FURenderKit.getInstance().setFaceBeauty(DefaultFaceBeautyDataFactory.currentBeauty);
            });
            put(CONFIG_SHANGTANG, () -> {
                FaceBeauty model = new FaceBeauty(new FUBundleData(DemoConfig.BUNDLE_FACE_BEAUTIFICATION));
                model.setFaceShapeIntensity(1.0);
                model.setFilterName(FaceBeautyFilterEnum.FENNEN_2);
                model.setFilterIntensity(0.8);
                model.setColorIntensity(0.7);
                model.setBlurIntensity(4.2);
                model.setEyeEnlargingIntensityV2(0.6);
                model.setCheekThinningIntensity(0.3);
                DefaultFaceBeautyDataFactory.currentBeauty = model;
                FURenderKit.getInstance().setFaceBeauty(DefaultFaceBeautyDataFactory.currentBeauty);
            });
            put(CONFIG_BIAOZHUN, () -> {
                FaceBeauty model = new FaceBeauty(new FUBundleData(DemoConfig.BUNDLE_FACE_BEAUTIFICATION));
                model.setFaceShapeIntensity(1.0);
                model.setFilterName(FaceBeautyFilterEnum.ZIRAN_5);
                model.setFilterIntensity(0.55);
                model.setColorIntensity(0.2);
                model.setRedIntensity(0.65);
                model.setBlurIntensity(3.3);
                model.setCheekSmallIntensityV2(0.05);
                model.setCheekThinningIntensity(0.1);
                DefaultFaceBeautyDataFactory.currentBeauty = model;
                FURenderKit.getInstance().setFaceBeauty(DefaultFaceBeautyDataFactory.currentBeauty);
            });
        }

    };

    /**
     * 克隆模型
     *
     * @param faceBeauty
     * @return
     */
    public static FaceBeauty clone(FaceBeauty faceBeauty) {
        FaceBeauty cloneFaceBeauty = new FaceBeauty(new FUBundleData(faceBeauty.getControlBundle().getPath()));
        /*滤镜*/
        cloneFaceBeauty.setFilterName(faceBeauty.getFilterName());
        cloneFaceBeauty.setFilterIntensity(faceBeauty.getFilterIntensity());
        /*美肤*/
        cloneFaceBeauty.setBlurIntensity(faceBeauty.getBlurIntensity());
        cloneFaceBeauty.setEnableHeavyBlur(faceBeauty.getEnableHeavyBlur());
        cloneFaceBeauty.setEnableSkinDetect(faceBeauty.getEnableSkinDetect());
        cloneFaceBeauty.setNonSkinBlurIntensity(faceBeauty.getNonSkinBlurIntensity());
        cloneFaceBeauty.setBlurType(faceBeauty.getBlurType());
        cloneFaceBeauty.setEnableBlurUseMask(faceBeauty.getEnableBlurUseMask());
        cloneFaceBeauty.setColorIntensity(faceBeauty.getColorIntensity());
        cloneFaceBeauty.setRedIntensity(faceBeauty.getRedIntensity());
        cloneFaceBeauty.setSharpenIntensity(faceBeauty.getSharpenIntensity());
        cloneFaceBeauty.setEyeBrightIntensity(faceBeauty.getEyeBrightIntensity());
        cloneFaceBeauty.setToothIntensity(faceBeauty.getToothIntensity());
        cloneFaceBeauty.setRemovePouchIntensity(faceBeauty.getRemovePouchIntensity());
        cloneFaceBeauty.setRemoveLawPatternIntensity(faceBeauty.getRemoveLawPatternIntensity());
        /*美型*/
        cloneFaceBeauty.setFaceShape(faceBeauty.getFaceShape());
        cloneFaceBeauty.setFaceShapeIntensity(faceBeauty.getFaceShapeIntensity());
        cloneFaceBeauty.setCheekThinningIntensity(faceBeauty.getCheekThinningIntensity());
        cloneFaceBeauty.setCheekVIntensity(faceBeauty.getCheekVIntensity());
        cloneFaceBeauty.setCheekLongIntensity(faceBeauty.getCheekLongIntensity());
        cloneFaceBeauty.setCheekCircleIntensity(faceBeauty.getCheekCircleIntensity());
        cloneFaceBeauty.setCheekNarrowIntensityV2(faceBeauty.getCheekNarrowIntensityV2());
        cloneFaceBeauty.setCheekShortIntensity(faceBeauty.getCheekShortIntensity());
        cloneFaceBeauty.setCheekSmallIntensityV2(faceBeauty.getCheekSmallIntensityV2());
        cloneFaceBeauty.setCheekBonesIntensity(faceBeauty.getCheekBonesIntensity());
        cloneFaceBeauty.setLowerJawIntensity(faceBeauty.getLowerJawIntensity());
        cloneFaceBeauty.setEyeEnlargingIntensityV2(faceBeauty.getEyeEnlargingIntensityV2());
        cloneFaceBeauty.setChinIntensity(faceBeauty.getChinIntensity());
        cloneFaceBeauty.setForHeadIntensityV2(faceBeauty.getForHeadIntensityV2());
        cloneFaceBeauty.setNoseIntensityV2(faceBeauty.getNoseIntensityV2());
        cloneFaceBeauty.setMouthIntensityV2(faceBeauty.getMouthIntensityV2());
        cloneFaceBeauty.setCanthusIntensity(faceBeauty.getCanthusIntensity());
        cloneFaceBeauty.setEyeSpaceIntensity(faceBeauty.getEyeSpaceIntensity());
        cloneFaceBeauty.setEyeRotateIntensity(faceBeauty.getEyeRotateIntensity());
        cloneFaceBeauty.setLongNoseIntensity(faceBeauty.getLongNoseIntensity());
        cloneFaceBeauty.setPhiltrumIntensity(faceBeauty.getPhiltrumIntensity());
        cloneFaceBeauty.setSmileIntensity(faceBeauty.getSmileIntensity());
        cloneFaceBeauty.setEyeCircleIntensity(faceBeauty.getEyeCircleIntensity());
        return cloneFaceBeauty;
    }

}
