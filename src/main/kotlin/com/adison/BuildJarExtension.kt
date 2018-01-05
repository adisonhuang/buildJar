package com.adison

/**
 * @param includePackage 需要输出jar的包名列表,当此参数为空时，则默认全项目输出
 * @param excludeJar 不需要输出jar的jar包
 * @param excludeClass 不需要输出jar的类名列表
 * @param excludePackage 不需要输出jar的包名列表
 * @param outputFileDir 输出目录
 * @param outputFileName 输出原始jar包名
 * @param outputProguardFileName 输出混淆jar包名
 * @param proguardConfigFile 混淆配置文件
 * @param applyMappingFile applyMapping
 * @param needDefaultProguard 是否需要默认的混淆配置proguard-android.txt
 */
open class BuildJarExtension(var includePackage: HashSet<String> = HashSet(), var excludeJar: HashSet<String> = HashSet(),
                             var excludeClass: HashSet<String> = HashSet(), var excludePackage: HashSet<String> = HashSet(),
                             var outputFileDir: String = "", var outputFileName: String = "",
                             var outputProguardFileName: String = "", var proguardConfigFile: String = "",
                             var applyMappingFile: String = "", var needDefaultProguard: Boolean = false)