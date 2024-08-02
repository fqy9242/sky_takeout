package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common/")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
	@Autowired
	private AliOssUtil aliOssUtil;
	@ApiOperation("文件上传")
	@PostMapping("/upload")
	public Result<String> upload(MultipartFile file) {
		log.info("文件上传:{}", file);
		// 获取原始文件名
		String originalFilename = file.getOriginalFilename();
		// 截取原始文件名的后缀
		String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		String fileName = UUID.randomUUID() + extension;
		String fileUrl = null;
		try {
			fileUrl = aliOssUtil.upload(file.getBytes(), fileName);
			return Result.success(fileUrl);
		} catch (IOException e) {
			log.error(e.toString());
		}
		return Result.error(MessageConstant.UPLOAD_FAILED);

	}

}
