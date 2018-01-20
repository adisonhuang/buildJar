# buildJar
**通用的打包jar gradle插件**

## 支持特性

1. 按需打包jar：
   * 全项目打包jar
   * 指定输出Jar包的包名路径列表
   * 过滤指定包名路径列表
   * 过滤指定class
   * 过滤指定jar
2. 支持混淆打包jar
3. 支持applymapping

## 使用说明

1. 引入依赖

   ```groovy
   dependencies {
           classpath 'com.android.tools.build:gradle:3.0.1'
           classpath 'com.adison.gradleplugin:jar:1.1.0'
       }
   ```
   ​

2. 应用插件

   ```groovy
   apply plugin: 'jar-gradle-plugin'
   BuildJar{
       //输出目录
       outputFileDir= project.buildDir.path+"/jar"
       //输出原始jar包名
       outputFileName="test.jar"
       //输出混淆jar包名
       outputProguardFileName="test_proguard.jar"
       //混淆配置
       proguardConfigFile="proguard-rules.pro"
       //是否需要默认的混淆配置proguard-android.txt
       needDefaultProguard=false
       applyMappingFile="originMapping/mapping.txt"
        //不需要输出jar的jar包列表,如['baidu.jar','baidu1.jar'...]
       excludeJar=[]
       //需要输出jar的包名列表,当此参数为空时，则默认全项目输出,支持多包,如 includePackage=['com/adison/testjarplugin/include','com/adison/testjarplugin/include1'...]
       includePackage=['com/adison/testjarplugin/include']
       //不需要输出jar的类名列表,如['baidu.calss','baidu1.class'...]
       excludeClass=['com/adison/testjarplugin/TestExcude.class']
       //不需要输出jar的包名列表,如 excludePackage=['com/adison/testjarplugin/exclude','com/adison/testjarplugin/exclude1'...]
       excludePackage=['com/adison/testjarplugin/exclude']
   }
   ```

   ​

   > 可参见[使用demo](https://github.com/adisonhyh/TestJarPlugin/)

   ## 版本记录
    - **1.1.0**

      fix bug

   - **1.0.3**

     使用kotlin重写

     兼容 Android studio3.0

   - **1.0.2**

     功能完善


   ## LICENSE

   Apache License 2.0

   ​