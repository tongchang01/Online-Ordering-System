package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/21
 * @time14:01 文件上传到云服务器 alioss
 * 这里2023javaweb视频里有
 * anli项目里也写过
 * <p>
 * 但是这里我就不上传到阿里云了alioss外网访问收费
 * 后期我是要把项目打包上传到外网可访问的云里
 * 所以这里直接保存到本地的项目目录里
 * 保存到本地项目中
 * 这个功能再瑞吉外卖中有使用
 * 但是后期要把路径换成Linux的路径就要两个路径了
 * 注意区分
 **/
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "图片文件上传")
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file);

        //此时会把该文件存到c盘的一个目录下作为临时文件
        //本次请求结束临时文件自动删除
        //所以需要把文件进行转存 先转存到本地

        //文件原名
        String Filename = file.getOriginalFilename();
        //获取原名中最后一个.的位置
        int i = Filename.lastIndexOf(".");
        //以.的位置分割出完整的后缀名
        String substring = Filename.substring(i);
        //生成uuid与后缀名拼接成新的文件名 避免文件重名
        String name = UUID.randomUUID().toString() + substring;

        //调用aliOssUtil里的方法上传文件到oss
        try {
            String path = aliOssUtil.upload(file.getBytes(),name);
            //该方法上传文件后会生成 该文件的访问路径
            return Result.success(path);
        } catch (IOException e) {
            log.info("文件上传失败{}",e);
        }

        /**
        //设置本地文件转存路径
        //这里是存到当前项目中的resources.static.img下

        //获取到当先项目的字节码文件位置target/classes
        ApplicationHome applicationHome = new ApplicationHome(this.getClass());
        //获取到当前项目的最顶级目录sky-take-out拼接下级目录
        String Path=applicationHome.getDir().getParentFile().getParentFile()
                .getAbsoluteFile()+"\\src\\main\\resources\\images\\";
        // 部署到服务器路径要变更 linux把\\换成//应该就可以了 到时候去试试看

        //把目录加上完整文件名=完整路径
        String path=Path+name;
         这里文件上传成功 但是前端回显失败
         */

        //要给前端返回该文件的完整路径 即完整路径加完整文件名 用于回显
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
