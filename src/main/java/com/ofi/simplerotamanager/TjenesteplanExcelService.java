package com.ofi.simplerotamanager;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.*;
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
        rowIndex = generateDataRows(tjenesteplan, wb, sheet, ansatte, ++rowIndex);
        generateTrailerRow(tjenesteplan, sheet, ansatte, rowIndex);

        boldHeaderRow(wb, sheet);
        colorWeekendRows(tjenesteplan, sheet);
        colorRow(sheet.getRow(sheet.getLastRowNum()), new XSSFColor(new java.awt.Color(255, 253, 118)));
        resizeColumnsToFitContent(sheet, totalNoOfColumns);
        return writeWorkbookToBytes(wb);
    }

    private void generateTitleRow(Tjenesteplan tjenesteplan, Workbook wb, XSSFSheet sheet, int totalNoOfColumns, int rowIndex) {
        XSSFRow titleRow = createDefaultStyleRow(sheet, rowIndex);
        titleRow.setHeight((short) 900);

        Cell cell = titleRow.createCell(0);
        cell.setCellValue(tjenesteplan.toString());

        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeight((short) 300);
        CellUtil.setFont(cell, font);
        CellUtil.setVerticalAlignment(cell, VerticalAlignment.CENTER);
        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);

        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, totalNoOfColumns - 1));
    }

    private void generateHeaderRow(List<String> ansatte, XSSFSheet sheet, int rowIndex) {
        XSSFRow header = createDefaultStyleRow(sheet, rowIndex);

        createDefaultStyleCell(header, 0).setCellValue("DAG");
        createDefaultStyleCell(header, 1).setCellValue("DATO");

        for (int ansattIndex = 0, columnIndex = 2; ansattIndex < ansatte.size(); ansattIndex++, columnIndex += 2) {
            createDefaultStyleCell(header, columnIndex).setCellValue(ansatte.get(ansattIndex));
            createDefaultStyleCell(header, columnIndex + 1).setCellValue("T");
        }
    }

    private int generateDataRows(Tjenesteplan tjenesteplan, Workbook wb, XSSFSheet sheet, List<String> ansatte, int rowIndex) {
        for (int dateIndex = 1; dateIndex <= tjenesteplan.getManed().length(Year.isLeap(tjenesteplan.getAar())); dateIndex++, rowIndex++) {
            XSSFRow row = createDefaultStyleRow(sheet, rowIndex);
            LocalDate date = LocalDate.of(tjenesteplan.getAar(), tjenesteplan.getManed(), dateIndex);

            insertDayOfWeekIntoCell(createDefaultStyleCell(row, 0), date);
            insertDateIntoCell(createDefaultStyleCell(row, 1), date, wb);

            for (int ansattIndex = 0, columnIndex = 2; ansattIndex < ansatte.size(); ansattIndex++, columnIndex += 2) {
                Cell cellSkifter = createDefaultStyleCell(row, columnIndex);
                List<Skift> skifter = tjenesteplan.getSkifterForAnsattForDato(ansatte.get(ansattIndex), date);

                cellSkifter.setCellValue(skifter.stream().map(Skift::toString).collect(joining(", ")));

                Duration totalDuration = Duration.ZERO;
                for (Skift skift : skifter) {
                    totalDuration = totalDuration.plus(skift.getDuration());
                }

                double hours = convertToHoursRoundedToNearestQuarter(totalDuration);
                Cell cellHours = createDefaultStyleCell(row, columnIndex + 1);
                if (hours != 0) {
                    cellHours.setCellValue(hours % 1 == 0 ? (int) hours : hours);
                }
            }
        }
        return rowIndex;
    }

    private void generateTrailerRow(Tjenesteplan tjenesteplan, XSSFSheet sheet, List<String> ansatte, int rowIndex) {
        XSSFRow trailerRow = createDefaultStyleRow(sheet, rowIndex);
        createDefaultStyleCell(trailerRow, 0).setCellValue("SUM");
        createDefaultStyleCell(trailerRow, 1);

        for (int ansattIndex = 0, columnIndex = 2; ansattIndex < ansatte.size(); ansattIndex++, columnIndex += 2) {
            createDefaultStyleCell(trailerRow, columnIndex);

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
            createDefaultStyleCell(trailerRow, columnIndex + 1).setCellValue(sumHours % 1 == 0 ? (int) sumHours : sumHours);
        }
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

    private void insertDateIntoCell(Cell cell, LocalDate date, Workbook wb) {
        CellStyle cellStyleDate = wb.createCellStyle();
        cellStyleDate.setDataFormat(wb.createDataFormat().getFormat(DateFormatConverter.convert(Locale.getDefault(), "dd. MMM")));
        cellStyleDate.setAlignment(HorizontalAlignment.CENTER);
        cellStyleDate.setBorderBottom(BorderStyle.THIN);
        cellStyleDate.setBorderTop(BorderStyle.THIN);
        cellStyleDate.setBorderLeft(BorderStyle.THIN);
        cellStyleDate.setBorderRight(BorderStyle.THIN);
        cell.setCellStyle(cellStyleDate);
        cell.setCellValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void insertDayOfWeekIntoCell(Cell cell, LocalDate date) {
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, App.LOCALE);
        cell.setCellValue(StringUtils.capitalize(dayOfWeek));
    }

    private XSSFRow createDefaultStyleRow(XSSFSheet sheet, int rowIndex) {
        XSSFRow row = sheet.createRow(rowIndex);
        row.setHeight((short) 350);
        return row;
    }

    private Cell createDefaultStyleCell(Row row, int columnIndex) {
        Cell cell = row.createCell(columnIndex);
        CellUtil.setAlignment(cell, HorizontalAlignment.CENTER);
        CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_LEFT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        return cell;
    }

    private void boldHeaderRow(Workbook wb, XSSFSheet sheet) {
        Font font = wb.createFont();
        font.setBold(true);

        int headerRowIndex = 1;
        XSSFRow row = sheet.getRow(headerRowIndex);
        for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
            CellUtil.setFont(row.getCell(cellNum), font);
        }
    }

    private void colorWeekendRows(Tjenesteplan tjenesteplan, XSSFSheet sheet) {
        for (int dateIndex = 1, rowNum = 2; dateIndex <= tjenesteplan.getManed().length(Year.isLeap(tjenesteplan.getAar())); dateIndex++, rowNum++) {
            LocalDate date = LocalDate.of(tjenesteplan.getAar(), tjenesteplan.getManed(), dateIndex);
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                colorRow(sheet.getRow(rowNum), new XSSFColor(new java.awt.Color(180, 231, 255)));
            } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                colorRow(sheet.getRow(rowNum), new XSSFColor(new java.awt.Color(255, 146, 133)));
            }
        }
    }

    private void colorRow(XSSFRow row, XSSFColor color) {
        for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
            XSSFCell cell = row.getCell(cellNum);
            XSSFCellStyle cellStyle = (XSSFCellStyle) cell.getCellStyle().clone();
            cellStyle.setFillForegroundColor(color);
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(cellStyle);
        }
    }

    private void resizeColumnsToFitContent(XSSFSheet sheet, int noOfColumns) {
        for (int columnIndex = 0; columnIndex <= noOfColumns; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
            sheet.setColumnWidth(columnIndex, sheet.getColumnWidth(columnIndex) + 300);
        }
    }

    private byte[] writeWorkbookToBytes(Workbook wb) throws IOException {
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
