package org.zero.drools.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.zero.drools.constant.DroolsConstant;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author Zero (cnzeropro@qq.com)
 * @since 2022/9/19
 */
@Slf4j
@UtilityClass
public class DroolsFileUtil {
    private static final ConcurrentMap<String, String> RULE_CACHE = new ConcurrentHashMap<>();

    public String put(String ruleFilePath, String ruleContent) {
        return RULE_CACHE.put(ruleFilePath, ruleContent);
    }

    public String get(String ruleFilePath) {
        return RULE_CACHE.get(ruleFilePath);
    }

    /**
     * 获取指定文件夹下所有文件
     */
    @SneakyThrows(Exception.class)
    public List<File> listRuleFiles(String rulePath) {
        List<File> ruleFiles = new ArrayList<>();
        if (rulePath.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(rulePath);
            ruleFiles.addAll(Arrays.stream(resources).map(Resource::getFile).collect(Collectors.toList()));
        } else {
            File file = new File(rulePath);
            if (file.exists()) {
                if (file.isFile()) {
                    ruleFiles.add(file);
                }
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (Objects.nonNull(files)) {
                        for (File nextFile : files) {
                            ruleFiles.addAll(listRuleFiles(nextFile.getPath()));
                        }
                    }
                }
            }
        }
        return ruleFiles;
    }

    public List<String> reload(String[] rulePaths, String charset) {
        if (ObjectUtils.isEmpty(rulePaths)) {
            return new ArrayList<>();
        }

        List<File> ruleFiles = new ArrayList<>();
        for (String rulePath : rulePaths) {
            ruleFiles.addAll(listRuleFiles(rulePath));
        }

        List<String> contents = new ArrayList<>();
        ruleFiles.forEach(ruleFile -> {
            String ruleContent = readFromFile(ruleFile, charset);
            if (StringUtils.hasText(ruleContent)) {
                contents.add(put(ruleFile.getPath(), ruleContent));
            }
        });

        return contents;
    }

    public String readFromFile(File ruleFile, String charset) {
        return readFromFile(ruleFile, Charset.forName(charset));
    }

    @SneakyThrows
    public String readFromFile(File ruleFile, Charset charset) {
        if (!ruleFile.exists()) {
            return "";
        }
        String fileName = ruleFile.getName();
        try (InputStream in = Files.newInputStream(ruleFile.toPath())) {
            // drl文件
            if (fileName.endsWith(DroolsConstant.SUFFIX_DRL)) {
                return readFromDrlFile(in, charset);
            }
            SpreadsheetCompiler spreadsheetCompiler = new SpreadsheetCompiler();
            // excel文件 xls和xlsx都支持
            if (fileName.endsWith(DroolsConstant.SUFFIX_EXCEL) || fileName.endsWith(DroolsConstant.SUFFIX_EXCEL_2007)) {
                return spreadsheetCompiler.compile(in, InputType.XLS);
            }
            // csv文件
            if (fileName.endsWith(DroolsConstant.SUFFIX_CSV)) {
                return spreadsheetCompiler.compile(in, InputType.CSV);
            }
        }
        return "";
    }

    @SneakyThrows
    public String readFromDrlFile(InputStream in, Charset charset) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    @SneakyThrows
    public String readFromDrlFile(Path drlPath, Charset charset) {
        return readFromDrlFile(Files.newInputStream(drlPath), charset);
    }

    public String readFromDrlFile(File drlFile, Charset charset) {
        return readFromDrlFile(drlFile.toPath(), charset);
    }

    public String readFromDrlFile(String drlFilePath, Charset charset) {
        return readFromDrlFile(new File(drlFilePath), charset);
    }
}