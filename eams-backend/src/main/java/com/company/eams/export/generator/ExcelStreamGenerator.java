package com.company.eams.export.generator;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class ExcelStreamGenerator implements AutoCloseable {

    private final SXSSFWorkbook workbook;
    private final SXSSFSheet sheet;
    private final CellStyle headerStyle;
    private final CellStyle dataStyle;
    private final CellStyle dateStyle;
    private int currentRowIndex = 0;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ExcelStreamGenerator(String sheetName, List<String> headers) {
        // Sliding window of 100 rows kept in memory, older rows flushed to disk
        this.workbook = new SXSSFWorkbook(100);
        this.workbook.setCompressTempFiles(true);
        this.sheet = workbook.createSheet(sheetName);

        // Header style
        this.headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(headerStyle);

        // Data style
        this.dataStyle = workbook.createCellStyle();
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(dataStyle);

        // Date style
        this.dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(dateStyle);

        // Write header row
        writeHeader(headers);
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void writeHeader(List<String> headers) {
        Row headerRow = sheet.createRow(currentRowIndex++);
        headerRow.setHeightInPoints(24);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    public void writeRow(List<?> values) {
        Row row = sheet.createRow(currentRowIndex++);
        row.setHeightInPoints(18);
        for (int i = 0; i < values.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(dataStyle);
            Object val = values.get(i);
            if (val == null) {
                cell.setCellValue("");
            } else if (val instanceof Number num) {
                cell.setCellValue(num.doubleValue());
            } else if (val instanceof Boolean b) {
                cell.setCellValue(b);
            } else if (val instanceof Instant instant) {
                cell.setCellValue(ISO_FORMATTER.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault())));
            } else if (val instanceof LocalDate localDate) {
                cell.setCellValue(localDate.toString());
            } else if (val instanceof Date d) {
                cell.setCellValue(d);
                cell.setCellStyle(dateStyle);
            } else {
                cell.setCellValue(val.toString());
            }
        }
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
