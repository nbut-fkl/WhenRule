package com.fkl.whenRule.condition.impl.holiday;

/**
 * 节假日数据访问异常。
 *
 * @author fkl
 */
public class HolidayDataAccessException extends RuntimeException {

    public HolidayDataAccessException(String message) {
        super(message);
    }

    public HolidayDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
