package com.hachimi.mamboaiplatform.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.hachimi.mamboaiplatform.ai.model.HtmlCodeResult;
import com.hachimi.mamboaiplatform.ai.model.MultiFileCodeResult;
import com.hachimi.mamboaiplatform.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static com.hachimi.mamboaiplatform.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;


/**
 * 将代码文件保存到本地的目录
 */
@Deprecated
public class CodeFileSaver {
    // 文件根目录设置
    private static final String BASE_DIR = CODE_OUTPUT_ROOT_DIR;

    /**
     * 保存html文件
     * @param htmlCodeResult
     * @return
     */
    public static File saveHtmlFile(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        saveFile(baseDirPath,"index.html", htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }

    /**
     * 保存多文件
     * @param multiFileCodeResult
     * @return
     */
    public static File saveMultiFile(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        saveFile(baseDirPath,"index.html", multiFileCodeResult.getHtmlCode());
        saveFile(baseDirPath,"style.css", multiFileCodeResult.getCssCode());
        saveFile(baseDirPath,"script.js", multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 文件路径生成规则 tmp/code_output/bizType_雪花id
     * @param bizType
     * @return
     */
    private static String buildUniqueDir(String bizType) {
        String uniqueDir = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextId());
        String dirPath = BASE_DIR + File.separator + uniqueDir;
        FileUtil.mkdir(dirPath); //创建文件路径
        return dirPath;
    }


    /**
     * 保存单个文件
     * @param dirPath
     * @param fileName
     * @param content
     * @return
     */
    private static void saveFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

}
