package com.go.util;

/**
 * Record类 记录一个棋子的颜色和位置
 */
public class Record {

    private int color; // 颜色
    private int i;		 // 横坐标
    private int j;       // 纵坐标

    /**
     * 构造器
     * @param color 颜色
     */
    public Record (int color) {
        this.color = color;
    }

    /**
     * 构造器
     * @param i 横坐标
     * @param j 纵坐标
     */
    public Record (int i, int j) {
        this.i = i;
        this.j = j;
    }

    /**
     * 获取横坐标
     */
    public int getI() {
        return i;
    }

    /**
     * 设置横坐标
     * @param i 横坐标
     */
    public void setI(int i) {
        this.i = i;
    }

    /**
     * 获取纵坐标
     */
    public int getJ() {
        return j;
    }

    /**
     * 设置纵坐标
     * @param j 纵坐标
     */
    public void setJ(int j) {
        this.j = j;
    }

    /**
     * 获取颜色
     */
    public int getColor() {
        return color;
    }

    /**
     * 设置颜色
     * @param color 颜色
     */
    public void setColor(int color) {
        this.color = color;
    }
}