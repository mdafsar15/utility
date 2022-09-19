package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.model.BirthModel;
import com.ndmc.ndmc_record.service.ExcelService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;


@Service
public class ExcelServiceImpl implements ExcelService {
    @Override
    public void createExcelOutputExcel(HttpServletResponse response, List objectBeans, String headers, String methodList) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
        // split methodList with |
        String[] methodListArray = methodList.split("\\|");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Datatypes in Java");

        int rowNum = 0;
        System.out.println("Creating excel");
        // declare method testId and testName from objectBean

        Row rowHead = sheet.createRow(rowNum++);
        int colNumHead = 0;
        for (String header : headers.split("\\|")) {
            Cell cell = rowHead.createCell(colNumHead++);
            cell.setCellValue(header);
        }

        for (Object objectBean : objectBeans) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;

            for (String str : methodListArray) {
                Method method = objectBean.getClass().getDeclaredMethod("get" + str);
                Object objvalue = method.invoke(objectBean, (Object[]) null);
                System.out.println(objvalue);
                Cell cell = row.createCell(colNum++);
                if (objvalue instanceof String) {
                    cell.setCellValue((String) objvalue);
                } else if (objvalue instanceof Integer) {
                    cell.setCellValue((Integer) objvalue);
                } else if (objvalue instanceof Long) {
                    cell.setCellValue((Long) objvalue);
                } else if (objvalue instanceof LocalDate) {
                    cell.setCellValue((String) ((LocalDate)objvalue).toString());
                } else if (objvalue instanceof LocalDateTime) {
                    cell.setCellValue((String) objvalue);
                } else if (objvalue instanceof Float) {
                    cell.setCellValue((Float) objvalue);
                } else if (objvalue instanceof Boolean) {
                    cell.setCellValue((Boolean) objvalue);
                } else if (objvalue instanceof BigInteger) {
                    cell.setCellValue((Integer) objvalue);
                }
            }
        }
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        System.out.println("Done");
    }

}
