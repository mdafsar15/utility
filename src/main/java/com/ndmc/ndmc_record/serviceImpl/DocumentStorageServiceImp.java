package com.ndmc.ndmc_record.serviceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.AttachmentDto;
import com.ndmc.ndmc_record.dto.UploadFileResponse;
import com.ndmc.ndmc_record.exception.DocumentNotFoundException;
import com.ndmc.ndmc_record.exception.FileStorageException;
import com.ndmc.ndmc_record.property.FileStorageProperties;
import com.ndmc.ndmc_record.service.DocumentStorageService;

@Service
public class DocumentStorageServiceImp implements DocumentStorageService{

	private final Logger logger = LoggerFactory.getLogger(DocumentStorageServiceImp.class);

	private Path fileStorageLocation;

	@Autowired
	public DocumentStorageServiceImp(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.");
		}
	}

	public ApiResponse storeFile(MultipartFile file, Long slaId, AttachmentDto attachmentDto) {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		String extension="";
		if (fileName.contains("."))
		     extension = 	fileName.substring(fileName.lastIndexOf("."));
		     fileName =		slaId+attachmentDto.getDocumentName()+"_"+extension;
		try (InputStream inputStream = file.getInputStream()){
			Path targetLocation = this.fileStorageLocation.resolve(fileName);
			Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
			String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/api/v1/document/downloadDocument/").path(fileName).toUriString();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setStatus(HttpStatus.OK);
			apiResponse.setMsg(Constants.FILE_UPLOAD_SUCCESS);
			apiResponse.setData(new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize(), file.hashCode(), attachmentDto.getDocumentName()));
			return apiResponse;
		
		} catch (IOException ex) {
			throw new FileStorageException("Could not store file " + fileName + ". Please try again!");
		}
	}

	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());

			// logger.info("=== Resource======"+resource);
			if (resource.exists()) {
				return resource;
			} else {
				throw new DocumentNotFoundException("File not found " + fileName);
			}
		} catch (Exception ex) {
			throw new DocumentNotFoundException("File not found " + fileName);
		}
	}

}