package com.company.eams.export.generator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvStreamGenerator implements AutoCloseable {

    private final OutputStreamWriter writer;
    private final CSVPrinter printer;

    public CsvStreamGenerator(OutputStream outputStream, List<String> headers) throws IOException {
        // Write UTF-8 BOM for Microsoft Excel auto-detection
        outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        this.writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(headers.toArray(new String[0]))
                .setRecordSeparator("\r\n")
                .build();
        this.printer = new CSVPrinter(writer, format);
    }

    public void writeRow(Object... values) throws IOException {
        printer.printRecord(values);
    }

    public void writeRow(List<?> values) throws IOException {
        printer.printRecord(values);
    }

    public void flush() throws IOException {
        printer.flush();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
        } finally {
            printer.close();
            writer.close();
        }
    }
}
