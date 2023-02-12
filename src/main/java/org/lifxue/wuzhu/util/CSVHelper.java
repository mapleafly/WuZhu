/*
 * Copyright 2020 lif.
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class CSVHelper {
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final char DELIMITER = ',';
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * @Description
     * @Author lifxue
     * @Date 2023/1/6 13:47
     * @Param [header, data, filePath]
     **/
    public static boolean writeCsv(String[] header, List<String[]> data, File filePath) {
        // 初始化csvformat
        CSVFormat formator =
            CSVFormat.DEFAULT
                .withHeader(header)
                .withRecordSeparator(NEW_LINE_SEPARATOR)
                .withDelimiter(DELIMITER);

        try {
            // 创建FileWriter对象
            FileWriter fileWriter = new FileWriter(filePath, CHARSET);
            // 创建CSVPrinter对象
            try (CSVPrinter printer = new CSVPrinter(fileWriter, formator)) {
                if (null != data) {
                    // 循环写入数据
                    printer.printRecords(data);
                }
            } catch (Exception e) {
                log.error(e.toString());
                return false;
            }
        } catch (IOException ex) {
            log.error(ex.toString());
            return false;
        }
        return true;
    }

    /**
     * @Description
     * @Author lifxue
     * @Date 2023/1/6 13:47
     * @param filePath
     * @return java.util.List<java.lang.String[]>
     **/
     public static List<String[]> readCsv(String filePath) {
        List<String[]> data = new ArrayList<>();
        try (Reader reader = new FileReader(filePath, CHARSET)) {
            Iterable<CSVRecord> records =
                CSVFormat.DEFAULT
                    // 第一行作为header
                    .withFirstRecordAsHeader()
                    .parse(reader);
            for (CSVRecord csvRecord : records) {
                String[] str = new String[csvRecord.size()];
                for (int i = 0; i < csvRecord.size(); i++) {
                    str[i] = csvRecord.get(i);
                }
                data.add(str);
            }
        } catch (IOException ex) {
            log.error(ex.toString());
        }
        return data;
    }

}
