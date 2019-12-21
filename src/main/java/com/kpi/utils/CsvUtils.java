package com.kpi.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public class CsvUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvUtils.class);

    public static CSVParser createCsvParser(Reader reader, String... headers) throws IOException {

        return new CSVParser(reader, CSVFormat.DEFAULT
                .withHeader(headers)
                .withDelimiter(',')
                .withIgnoreHeaderCase()
                .withSkipHeaderRecord()
                .withTrim());
    }

    public static void writeDataToCsv(List<Object> record, Writer writer) {
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord(record);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
