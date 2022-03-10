package com.vhall.opensdk.watchplayback;

public class PointInfo {


    /**
     * msg : 第一张截图文字描述
     * picurl : /vhallyun/vhallimage/pointframe/ac23cf44/20201030144935/img-000010.jpg
     * timePoint : 10
     */

    public String msg;
    public String picurl;
    public int timePoint;

    @Override
    public String toString() {
        return "PointInfo{" +
                "msg='" + msg + '\'' +
                ", picurl='" + picurl + '\'' +
                ", timePoint=" + timePoint +
                '}';
    }
}
