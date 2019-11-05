package cn.stylefeng.guns.modular.activiti.util;

/**
 * @author xuyuxiang
 * @name: TimeUtil
 * @description: 时间工具类
 * @date 2019/11/417:26
 */
public class TimeUtil {
    public static String formatDuring(long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        long millis = mss % 1000;
        return days + " 天 " + hours + " 时 " + minutes + " 分 "
                + seconds + " 秒 " + millis + "毫秒";
    }
}
