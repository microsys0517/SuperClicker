package com.superclicker.model;
public enum StepType {
    CLICK("点击", "点击指定坐标", Category.ACTION),
    SWIPE("滑动", "从A点滑到B点", Category.ACTION),
    LONG_PRESS("长按", "长按指定坐标", Category.ACTION),
    MULTI_TOUCH("多点触控", "同时多点操作", Category.ACTION),
    IMAGE_MATCH("识图", "查找屏幕上的图片", Category.RECOGNIZE),
    COLOR_MATCH("识色", "查找指定颜色", Category.RECOGNIZE),
    TEXT_MATCH("识字", "OCR识别文字", Category.RECOGNIZE),
    NUMBER_COMPARE("数字对比", "比较数值大小", Category.CONDITION),
    TEXT_COMPARE("文字对比", "比较文字内容", Category.CONDITION),
    TIME_COMPARE("时间对比", "比较时间", Category.CONDITION),
    COUNTER("计数", "计数器操作", Category.TOOL),
    INPUT_TEXT("输入文本", "输入指定文字", Category.TOOL),
    STOP("停止", "停止脚本执行", Category.CONTROL),
    PAUSE("暂停", "暂停执行", Category.CONTROL),
    BACK_KEY("返回键", "按下返回键", Category.CONTROL),
    HOME_KEY("桌面键", "按下Home键", Category.CONTROL),
    MENU_KEY("菜单键", "按下菜单键", Category.CONTROL),
    JUMP("跳转", "跳转到指定步骤", Category.CONTROL),
    EMPTY("空", "空操作/延时", Category.CONTROL),
    SUB_RULE("子规则", "调用其他脚本", Category.CONTROL);
    public final String label;
    public final String description;
    public final Category category;
    StepType(String label, String description, Category category) {
        this.label = label; this.description = description; this.category = category;
    }
    public enum Category {
        ACTION("操作"), RECOGNIZE("识别"), CONDITION("判断"), TOOL("工具"), CONTROL("控制");
        public final String label;
        Category(String label) { this.label = label; }
    }
}
