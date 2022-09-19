package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.model.BirthModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface ExcelService {
    public void createExcelOutputExcel(HttpServletResponse response, List objectBeans, String headers, String methodList) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException;
}
