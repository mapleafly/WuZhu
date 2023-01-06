/*
 * Copyright 2019 xuelf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifxue.wuzhu.util;

import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
  * @classname DateHelper
  * @description 日期助手
  * @auhthor lifxue
  * @date 2023/1/6 14:04
  * @version 1.0
*/
public class DateHelper {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final StringConverter<LocalDate> CONVERTER =
        new StringConverter<>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * @param date 1
     * @Description:
     * @return: java.lang.String formatted string
     * @author: mapleaf
     * @date: 2020/6/23 18:05
     */
    public static String toString(LocalDate date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMATTER.format(date);
    }

    /**
     * @param date 1
     * @Description: String转成LocalDate
     * @return: the date object or null if it could not be converted
     * @author: mapleaf
     * @date: 2020/6/23 18:07
     */
    public static LocalDate fromString(String date) {
        try {
            return DATE_FORMATTER.parse(date, LocalDate::from);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * @param date 1
     * @Description: valid date
     * @return: boolean true if the String is a valid date
     * @author: mapleaf
     * @date: 2020/6/23 18:07
     */
    public static boolean validDate(String date) {
        return DateHelper.fromString(date) != null;
    }

    public static String utcToLocal(String utc) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return DATE_FORMATTER.format(format.parse(utc));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * @param newDate 1
     * @param oldDate 2
     * @Description: 计算两个日期相差的天数 newDate - oldDate
     * @return: java.lang.Long
     * @author: mapleaf
     * @date: 2020/6/23 18:08
     */
    public static Long differentDays(LocalDate newDate, LocalDate oldDate) {
        return newDate.toEpochDay() - oldDate.toEpochDay();
    }
}
