import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {
    public static void main(String[] args) {
        // 自定义输出目录
        String projectPath = System.getProperty("user.dir");
        String outputDir = projectPath + "/src/main/java";
        String mapperXmlDir = projectPath + "/src/main/resources/mapper";

        FastAutoGenerator.create("jdbc:mysql://localhost:3306/elainachat", "root", "123456")
                // 全局配置
                .globalConfig((scanner, builder) -> builder
                        .author(scanner.apply("请输入作者名称？"))
                        .outputDir(outputDir)  // 设置代码输出目录
                        .disableOpenDir()  // 禁止打开输出目录
                )
                // 包配置
                .packageConfig((scanner, builder) -> builder
                        .parent(scanner.apply("请输入包名？"))
                        .pathInfo(getPathInfo(outputDir, mapperXmlDir))  // 设置自定义路径
                )
                // 策略配置
                .strategyConfig((scanner, builder) -> builder
                        .addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        .entityBuilder()
                        .enableLombok()
                        .addTableFills(
                                new Column("create_time", FieldFill.INSERT)
                        )
                        .build())
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }

    // 设置自定义输出路径
    private static Map<OutputFile, String> getPathInfo(String outputDir, String mapperXmlDir) {
        Map<OutputFile, String> pathInfo = new HashMap<>();
        pathInfo.put(OutputFile.xml, mapperXmlDir);
        return pathInfo;
    }
}