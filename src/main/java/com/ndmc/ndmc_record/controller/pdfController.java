//package com.ndmc.ndmc_record.controller;
//
//import com.lowagie.text.DocumentException;
//import com.ndmc.ndmc_record.serviceImpl.PdfGenaratorUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.HashMap;
//import java.util.Map;
//
//
//@RestController
//public class pdfController {
//	@Autowired
//    PdfGenaratorUtil pdfGenaratorUtil;
//
//@GetMapping("/pdf")
//public void pdfDownload() throws Exception
//{
//
//	Map<String,String> data = new HashMap<String,String>();
//    data.put("name","James");
// //   pdfGenaratorUtil.createPdf("students",data);
//    Path file = Paths.get(pdfGenaratorUtil.createPdf("students",data).getAbsolutePath());
//
//}
//
//@GetMapping("/download-pdf")
//public void downloadPDFResource(HttpServletResponse response) throws Exception {
//    try {
//    	Map<String,String> data = new HashMap<String,String>();
//        data.put("name","James");
//        Path file = Paths.get(pdfGenaratorUtil.createPdf("students",data).getAbsolutePath());
//        System.out.println("absolute file path -------  "+file);
//        if (Files.exists(file)) {
//            response.setContentType("application/pdf");
//            response.addHeader("Content-Disposition",
//                    "attachment; filename=" + file.getFileName());
//            Files.copy(file, response.getOutputStream());
//            response.getOutputStream().flush();
//           // pdfGenaratorUtil.uploadFile(response, "childDetails");
//        }
//    } catch (DocumentException | IOException ex) {
//        ex.printStackTrace();
//    }
//
//}
//}
//
