package com.ofi.simplerotamanager;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.DateFormatConverter;
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
        customizePrintSetup(sheet);

        List<String> ansatte = tjenesteplan.getAnsatte();

        int totalNoOfColumns = (ansatte.size() * 2) + 2;
        int rowIndex = 0;

        generateTitleRow(tjenesteplan, wb, sheet, totalNoOfColumns, rowIndex);
        generateHeaderRow(ansatte, sheet, ++rowIndex);

        rowIndex++;
        for (int dateIndex = 1; dateIndex <= tjenesteplan.getManed().length(Year.isLeap(tjenesteplan.getAar())); dateIndex++, rowIndex++) {
            XSSFRow row = createCommonStyleRow(sheet, rowIndex);
            LocalDate date = LocalDate.of(tjenesteplan.getAar(), tjenesteplan.getManed(), dateIndex);

            insertDayOfWeekIntoCell(createCommonStyleCell(row, 0), date);
            insertDateIntoCell(createCommonStyleCell(row, 1), date, wb);

            for (int ansattIndex = 0, columnIndex = 2; ansattIndex < ansatte.size(); ansattIndex++, columnIndex += 2) {
                Cell cellSkifter = createCommonStyleCell(row, columnIndex);
                List<Skift> skifter = tjenesteplan.getSkifterForAnsattForDato(ansatte.get(ansattIndex), date);

                cellSkifter.setCellValue(skifter.stream().map(Skift::toString).collect(joining(", ")));

                Duration totalDuration = Duration.ZERO;
                for (Skift skift : skifter) {
                    totalDuration = totalDuration.plus(skift.getDuration());
                }

                double hours = convertToHoursRoundedToNearestQuarter(totalDuration);
                if (hours != 0) {
                    createCommonStyleCell(row, columnIndex + 1).setCellValue(hours % 1 == 0 ? (int) hours : hours);
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

        resizeColumnsToFitContent(sheet, totalNoOfColumns);
        return writeExcelToBytes(wb);
    }

    private void customizePrintSetup(XSSFSheet sheet) {
        double margin = 0.1;
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
        sheet.setVerticallyCenter(true);
        sheet.setMargin(Sheet.LeftMargin, margin);
        sheet.setMargin(Sheet.RightMargin, margin);
        sheet.setMargin(Sheet.TopMargin, margin);
        sheet.setMargin(Sheet.BottomMargin, margin);
        sheet.getPrintSetup().setFooterMargin(margin);
        sheet.getPrintSetup().setHeaderMargin(margin);
        sheet.getPrintSetup().setOrientation(PrintOrientation.LANDSCAPE);
        sheet.getPrintSetup().setPaperSize(PaperSize.A4_PAPER);
    }

    private void generateTitleRow(Tjenesteplan tjenesteplan, Workbook wb, XSSFSheet sheet, int totalNoOfColumns, int rowIndex) {
        XSSFRow titleRow = createCommonStyleRow(sheet, rowIndex);
        titleRow.setHeight((short) 900);

        Cell cell = createCommonStyleCell(titleRow, 0);
        cell.setCellValue(tjenesteplan.toString());

        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeight((short) 300);
        CellUtil.setFont(cell, font);
        CellUtil.setVerticalAlignment(cell, VerticalAlignment.CENTER);

        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, totalNoOfColumns - 1));
    }

    private void generateHeaderRow(List<String> ansatte, XSSFSheet sheet, int rowIndex) {
        XSSFRow header = createCommonStyleRow(sheet, rowIndex);

        createCommonStyleCell(header, 0).setCellValue("DAG");
        createCommonStyleCell(header, 1).setCellValue("DATO");

        for (int ansattIndex = 0, columnIndex = 2; ansattIndex < ansatte.size(); ansattIndex++, columnIndex += 2) {
            createCommonStyleCell(header, columnIndex).setCellValue(ansatte.get(ansattIndex));
            createCommonStyleCell(header, columnIndex + 1).setCellValue("T");
        }
    }

    private void generateTrailerRow(Tjenesteplan tjenesteplan, XSSFSheet sheet, List<String> ansatte, int rowIndex) {
        XSSFRow trailerRow = createCommonStyleRow(sheet, rowIndex);
        createCommonStyleCell(trailerRow, 0).setCellValue("SUM");

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
            createCommonStyleCell(trailerRow, columnIndex).setCellValue(sumHours % 1 == 0 ? (int) sumHours : sumHours);
        }
    }

    private void insertDateIntoCell(Cell cell, LocalDate date, Workbook wb) {
        CellStyle cellStyleDate = wb.createCellStyle();
        cellStyleDate.setDataFormat(wb.createDataFormat().getFormat(DateFormatConverter.convert(Locale.getDefault(), "dd.MM.yy")));
        cellStyleDate.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(cellStyleDate);
        cell.setCellValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void insertDayOfWeekIntoCell(Cell cell, LocalDate date) {
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, App.LOCALE);
        cell.setCellValue(StringUtils.capitalize(dayOfWeek));
    }

    private XSSFRow createCommonStyleRow(XSSFSheet sheet, int rowIndex) {
        XSSFRow row = sheet.createRow(rowIndex);
        row.setHeight((short) 350);
        return row;
    }

    private Cell createCommonStyleCell(Row row, int columnIndex) {
        Cell cell = row.createCell(columnIndex);
        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
        return cell;
    }

    private void resizeColumnsToFitContent(XSSFSheet sheet, int noOfColumns) {
        for (int columnIndex = 0; columnIndex <= noOfColumns; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
            sheet.setColumnWidth(columnIndex, sheet.getColumnWidth(columnIndex) + 300);
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
