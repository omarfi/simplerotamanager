package com.ofi.simplerotamanager;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Service
public class TjenesteplanExcelService {


    byte[] writeTjenesteplanToExcel(Tjenesteplan tjenesteplan) throws IOException {

        Workbook wb = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) wb.createSheet();

/*
        CTTable cttable = sheet.createTable().getCTTable();
        CTTableStyleInfo tableStyle = cttable.addNewTableStyleInfo();
        tableStyle.setName("TableStyleMedium9");
        tableStyle.setShowColumnStripes(false);
        tableStyle.setShowRowStripes(true);

        cttable.setRef(new AreaReference(new CellReference(0, 0), new CellReference(rows.size(), reportType.getColumns().size() - 1)).formatAsString());
        cttable.setDisplayName(reportType.toString());
        cttable.setName(reportType.toString());
        cttable.setId(1L);

        CTTableColumns columns = cttable.addNewTableColumns();
        cttable.addNewAutoFilter();
        columns.setCount(reportType.getColumns().size());

        XSSFRow heading = sheet.createRow((short) 0);

        for (int columnIndex = 0; columnIndex < reportType.getColumns().size(); columnIndex++) {
            String column = reportType.getColumns().get(columnIndex);

            CTTableColumn ctTableColumn = columns.addNewTableColumn();
            ctTableColumn.setName(column);
            ctTableColumn.setId(columnIndex + 1L);
            heading.createCell(columnIndex).setCellValue(column);
        }
*/

        List<String> ansatte = tjenesteplan.getAnsatte();

        generateHeaderRow(ansatte, sheet);

        int rowIndex;
        for (rowIndex = 1; rowIndex <= tjenesteplan.getManed().length(Year.isLeap(tjenesteplan.getAar())); rowIndex++) {
            XSSFRow row = sheet.createRow(rowIndex);
            LocalDate date = LocalDate.of(tjenesteplan.getAar(), tjenesteplan.getManed(), rowIndex);

            insertDayOfWeekIntoCell(row.createCell(0), date);
            insertDateIntoCell(row.createCell(1), date, wb);

            for (int ansattIndex = 0, columnIndex = 2; ansattIndex < ansatte.size(); ansattIndex++, columnIndex += 2) {
                XSSFCell cellSkifter = row.createCell(columnIndex);
                List<Skift> skifter = tjenesteplan.getSkifterForAnsattForDato(ansatte.get(ansattIndex), date);

                cellSkifter.setCellValue(skifter.stream().map(Skift::toString).collect(joining(",")));

                Duration totalDuration = Duration.ZERO;
                for (Skift skift : skifter) {
                    totalDuration = totalDuration.plus(skift.getDuration());
                }

                double hours = convertToHoursRoundedToNearestQuarter(totalDuration);
                if (hours != 0) {
                    row.createCell(columnIndex + 1).setCellValue(hours % 1 == 0 ? (int) hours : hours);
                }
            }
        }

        generateTrailerRow(tjenesteplan, sheet, ansatte, rowIndex);

       /* CellStyle cellStyleDate = wb.createCellStyle();
        cellStyleDate.setDataFormat(wb.createDataFormat().getFormat(DateFormatConverter.convert(Locale.getDefault(), "hh:MM:ss")));

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            XSSFRow xssfRow = sheet.createRow(rowIndex + 1);
            List<String> values = rows.get(rowIndex).getValues();

            for (int columnIndex = 0; columnIndex < values.size(); columnIndex++) {
                Cell cell = xssfRow.createCell(columnIndex);

                Object formattedValue = reportType.getDataFormatter().formatValue(reportType.getColumns().get(columnIndex), values.get(columnIndex));

                if (formattedValue instanceof Date) {
                    cell.setCellValue((Date) formattedValue);
                    cell.setCellStyle(cellStyleDate);
                } else if (formattedValue instanceof Integer) {
                    cell.setCellValue((int) formattedValue);
                } else {
                    cell.setCellValue((String) formattedValue);
                }
            }
        }*/

        resizeColumns(sheet, ansatte.size());
        return writeExcelToBytes(wb);
    }

    private void generateHeaderRow(List<String> ansatte, XSSFSheet sheet) {
        XSSFRow header = sheet.createRow(0);

        header.createCell(0).setCellValue("DAG");
        header.createCell(1).setCellValue("DATO");

        for (int ansattIndex = 0, columnIndex = 2; ansattIndex < ansatte.size(); ansattIndex++, columnIndex += 2) {
            header.createCell(columnIndex).setCellValue(ansatte.get(ansattIndex));
            header.createCell(columnIndex + 1).setCellValue("T");
        }
    }

    private void generateTrailerRow(Tjenesteplan tjenesteplan, XSSFSheet sheet, List<String> ansatte, int rowIndex) {
        XSSFRow trailerRow = sheet.createRow(rowIndex);
        trailerRow.createCell(0).setCellValue("SUM");

        for (int ansattIndex = 0, columnIndex = 3; ansattIndex < ansatte.size(); ansattIndex++, columnIndex += 2) {
            List<Duration> skiftDuartions = tjenesteplan
                    .getSkifterForAnsatt(ansatte.get(ansattIndex))
                    .stream()
                    .map(Skift::getDuration)
                    .collect(Collectors.toList());

            Duration totalDuration = Duration.ZERO;
            for (Duration duration : skiftDuartions) {
                totalDuration = totalDuration.plus(duration);
            }

            double sumHours = convertToHoursRoundedToNearestQuarter(totalDuration);
            trailerRow.createCell(columnIndex).setCellValue(sumHours % 1 == 0 ? (int) sumHours : sumHours);
        }
    }

    private void insertDateIntoCell(XSSFCell cell, LocalDate date, Workbook wb) {
        CellStyle cellStyleDate = wb.createCellStyle();
        cellStyleDate.setDataFormat(wb.createDataFormat().getFormat(DateFormatConverter.convert(Locale.getDefault(), "dd.MM.yy")));
        cell.setCellStyle(cellStyleDate);
        cell.setCellValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void insertDayOfWeekIntoCell(XSSFCell cell, LocalDate date) {
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, App.LOCALE);
        cell.setCellValue(StringUtils.capitalize(dayOfWeek));
    }

    private void resizeColumns(XSSFSheet sheet, int noOfColumns) {
        for (int columnIndex = 0; columnIndex <= noOfColumns; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
            sheet.setColumnWidth(columnIndex, sheet.getColumnWidth(columnIndex) + 700);
        }
    }

    private byte[] writeExcelToBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        wb.write(byteArrayOutputStream);
        wb.close();
        return byteArrayOutputStream.toByteArray();

    }

    static double convertToHoursRoundedToNearestQuarter(Duration duration) {
        long hours = duration.toHours();
        long minutePart = duration.minusHours(hours).toMinutes();

        double fractionOfHour = 0;
        if (minutePart > 45) {
            hours++;
        } else if (minutePart > 30) {
            fractionOfHour = 0.75;
        } else if (minutePart > 15) {
            fractionOfHour = 0.5;
        } else if (minutePart > 0) {
            fractionOfHour = 0.25;
        }

        return hours + fractionOfHour;
    }


}
