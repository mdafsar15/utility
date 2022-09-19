package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.blockchainGatway.BlockchainGatway;
import com.ndmc.ndmc_record.dto.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Component
public class PdfGenaratorUtil {
	 private static final Logger logger = LoggerFactory.getLogger(PdfGenaratorUtil.class);;
	@Autowired
	private TemplateEngine templateEngine;
	@Autowired
	private BlockchainGatway blockchain;

	public Response createPdf(String templateName, Map map, String registrationNumber, String createdBy, String createdAt) throws Exception {
		System.out.println("pdf generate method -------  "+registrationNumber);
		final File outputFile;
		Response blockchainResp=new Response();

		String message;
		Assert.notNull(templateName, "The templateName can not be null");
		Context ctx = new Context();
		if (map != null) {
		     Iterator itMap = map.entrySet().iterator();
		       while (itMap.hasNext()) {
			  Map.Entry pair = (Map.Entry) itMap.next();
		          ctx.setVariable(pair.getKey().toString(), pair.getValue());
			}
		}

		String processedHtml = templateEngine.process(templateName, ctx);
		  FileOutputStream os = null;
		  String fileName = registrationNumber;
	        try {
               outputFile=new File("/home/welcome/Desktop/ndmc_record/pdfFiles/"+fileName+".pdf");
	         //   outputFile = File.createTempFile(fileName, ".pdf",null);
	            System.out.println("out put ----------------  "+outputFile);
	            os = new FileOutputStream(outputFile);
	            System.out.println("os ---------  "+os);
	            ITextRenderer renderer = new ITextRenderer();
	            renderer.setDocumentFromString(processedHtml);
	            renderer.layout();
	            renderer.createPDF(os, false);
	            renderer.finishPDF();
				System.out.println("PDF created successfully");
				String filePath= String.valueOf(outputFile);
				System.out.println(filePath);
			//	message = blockchain.insertCertificate(registrationNumber,filePath,createdBy,createdAt);
				message = blockchain.approveRecord(registrationNumber,filePath);
				blockchainResp.setMsg(message);
	        }
	        finally {
	            if (os != null) {
	                try {
	                    os.close();
	                } catch (IOException e) { /*ignore*/ }
	            }
	        }
			return blockchainResp;
	}

}