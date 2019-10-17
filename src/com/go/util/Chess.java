package com.go.util;

/**
 * Chess类 代表棋子
 * 负责记录颜色和绘制自己
 */
public class Chess {

    private int color; // 棋子颜色

    /**
     * 构造器
     * @param color 棋子颜色
     */
    public Chess (int color) {
        this.color = color;
    }

    /**
     * 获取该棋子颜色
     */
    public int getColor () {
        return color;
    }

    /**
     * 设置该棋子颜色
     * @param color 颜色
     */
    public void setColor(int color) {
        this.color = color;
    }
    
}