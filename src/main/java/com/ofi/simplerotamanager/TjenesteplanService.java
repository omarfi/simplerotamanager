package com.ofi.simplerotamanager;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.joining;

@Service
public class TjenesteplanService {
    byte[] genererTjenesteplan(Tjenesteplan tjenesteplan) throws IOException {

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

        genererHeaderRad(ansatte, sheet);

        for (int rowIndex = 1; rowIndex <= tjenesteplan.getManed().length(Year.isLeap(tjenesteplan.getAar())); rowIndex++) {
            XSSFRow row = sheet.createRow(rowIndex);

            XSSFCell datoCelle = row.createCell(0);
            CellStyle cellStyleDate = wb.createCellStyle();
            cellStyleDate.setDataFormat(wb.createDataFormat().getFormat(DateFormatConverter.convert(Locale.getDefault(), "dd.MM.yy")));
            datoCelle.setCellStyle(cellStyleDate);

            LocalDate dato = LocalDate.of(tjenesteplan.getAar(), tjenesteplan.getManed(), rowIndex);
            datoCelle.setCellValue(Date.from(dato.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            for (int columnIndex = 1; columnIndex <= ansatte.size(); columnIndex++) {
                XSSFCell cell = row.createCell(columnIndex);
                List<Skift> skifter = tjenesteplan.getSkifterForAnsattForDato(ansatte.get(columnIndex - 1), dato);

                cell.setCellValue(skifter.stream().map(Skift::toString).collect(joining(",")));

                Duration totalDuration = Duration.ZERO;
                for (Skift skift : skifter) {
                    totalDuration = totalDuration.plus(skift.getDuration());
                }

                double antallTimer = konverterTilTimerRundetAvTilNaermesteKvarter(totalDuration);
            }


        }


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

    private void resizeColumns(XSSFSheet sheet, int noOfColumns) {
        for (int columnIndex = 0; columnIndex <= noOfColumns; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
            sheet.setColumnWidth(columnIndex, sheet.getColumnWidth(columnIndex) + 700);
        }
    }

    private void genererHeaderRad(List<String> ansatte, XSSFSheet sheet) {
        XSSFRow header = sheet.createRow(0);

        for (int columnIndex = 0; columnIndex < ansatte.size(); columnIndex++) {
            XSSFCell cell = header.createCell(columnIndex + 1);
            cell.setCellValue(ansatte.get(columnIndex));
        }
    }

    private byte[] writeExcelToBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        wb.write(byteArrayOutputStream);
        wb.close();
        return byteArrayOutputStream.toByteArray();

    }

    static double konverterTilTimerRundetAvTilNaermesteKvarter(Duration duration) {
        long timer = duration.toHours();
        long minuttDel = duration.minusHours(timer).toMinutes();

        double brokdelAvHelTime = 0;
        if (minuttDel > 45) {
            timer++;
        } else if (minuttDel > 30) {
            brokdelAvHelTime = 0.75;
        } else if (minuttDel > 15) {
            brokdelAvHelTime = 0.5;
        } else if (minuttDel > 0) {
            brokdelAvHelTime = 0.25;
        }

        return timer + brokdelAvHelTime;
    }


}
