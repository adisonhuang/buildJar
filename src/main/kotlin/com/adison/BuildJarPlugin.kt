package com.adison

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import com.android.SdkConstants
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.bundling.Jar
import proguard.gradle.ProGuardTask
import java.io.File

class BuildJarPlugin : Plugin<Project> {
    companion object {
        const val PLUGIN_EXTENSION_NAME = "BuildJar"
    }

    private lateinit var project: Project
    private lateinit var android: BaseExtension
    private lateinit var includePackage: HashSet<String>
    private lateinit var excludeClass: HashSet<String>
    private lateinit var excludePackage: HashSet<String>
    private lateinit var excludeJar: HashSet<String>
    override fun apply(target: Project?) {
        target?.let {
            project = target
            var appPlugin = target.plugins.hasPlugin(AppPlugin::class.java)
            var libPlugin = target.plugins.hasPlugin(LibraryPlugin::class.java)
            if ((appPlugin) || libPlugin) {
                android = target.extensions.getByName("android") as BaseExtension
            } else {
                throw ProjectConfigurationException("plugin 'com.android.application' or 'com.android.library'  must be apply", null)
            }
            target.extensions.create(PLUGIN_EXTENSION_NAME, BuildJarExtension::class.java)
        }
        target?.afterEvaluate {
            applyTask()
        }

    }

    private fun applyTask() {
//        buildTypes.all {
//            if (it.name == "Debug") {
        val dexTask = project.tasks.findByName("transformClassesWithDexBuilderForDebug")
        if (dexTask != null) {
            val buildJarBeforeDex = "buildJarBeforeDexDebug"
            val jarExtension = project.extensions.findByType(BuildJarExtension::class.java)
            includePackage = jarExtension.includePackage
            excludeClass = jarExtension.excludeClass
            excludePackage = jarExtension.excludePackage
            excludeJar = jarExtension.excludeJar
            val buildJar = project.tasks.create("buildJar", Jar::class.java)
            buildJar.description = "构建jar包"
            val buildJarBeforeDexTask = project.task(buildJarBeforeDex)
            //过滤R文件和BuildConfig文件
            buildJar.exclude("**/BuildConfig.class")
            buildJar.exclude("**/BuildConfig\$*.class")
            buildJar.exclude("**/R.class")
            buildJar.exclude("**/R\$*.class")
            buildJar.archiveName = jarExtension.outputFileName
            buildJar.destinationDir = project.file(jarExtension.outputFileDir)
            buildJar.includeEmptyDirs=true
            val fromFiles: MutableList<Any> = mutableListOf()
            buildJarBeforeDexTask.let {
                val inputFiles = dexTask.inputs.files.files
                inputFiles.forEach { inputFile ->
                    var path = inputFile.absolutePath
//                    project.logger.log(LogLevel.WARN, "inputFiles:::" + path)
                    if (path.endsWith(SdkConstants.DOT_JAR) && shouldFromJar(path)) {
//                        project.logger.log(LogLevel.WARN, "inputFiles:::DOT_JAR")
//                        try {
//                            for (file in project.zipTree(path).files) {
//                                project.logger.log(LogLevel.WARN, "inputFiles:::" + file)
//                                if (shouldFromClass(file.absolutePath)) {
//                                    fromFiles.add(file)
//                                }
//                            }
//                        } catch (e: Exception) {
//                        }
                        fromFiles.add(project.zipTree(path))
                    }
//                    else if (shouldFromClass(path)) {
////                        project.logger.log(LogLevel.WARN, "inputFiles:::else")
////                                buildJar.from(project.file(inputFile))
//                        fromFiles.add(path)
//                    }
                }
            }

            buildJar.from(fromFiles,"build/intermediates/classes/debug/")
//            excludeClass.forEach {
//                //排除指定class
//                buildJar.exclude(it)
//            }
//            excludePackage.forEach {
//                //过滤指定包名下class
////                project.logger.log(LogLevel.WARN,"$it/**/*.class")
//                buildJar.exclude("$it/**/*.class")
//            }
//            includePackage.forEach {
//                //仅仅打包指定包名下class
//                project.logger.log(LogLevel.WARN, "$it" + File.separatorChar + "**" + File.separatorChar + "*.class")
//                  buildJar.include("$it/**/*.class")
//            }


            var buildProguardJar = project.tasks.create("buildProguardJar", ProGuardTask::class.java)
            buildProguardJar.description = "混淆jar包"
            buildProguardJar.dependsOn(buildJar)
            //设置不删除未引用的资源(类，方法等)
            buildProguardJar.dontshrink()
            //忽略警告
            buildProguardJar.ignorewarnings()
            //需要被混淆的jar包
            buildProguardJar.injars(jarExtension.outputFileDir + "/" + jarExtension.outputFileName)
            //混淆后输出的jar包
            buildProguardJar.outjars(jarExtension.outputFileDir + "/" + jarExtension.outputProguardFileName)

            //libraryjars表示引用到的jar包不被混淆
            // ANDROID PLATFORM
            buildProguardJar.libraryjars(android.sdkDirectory.toString() + "/platforms/" + android.compileSdkVersion + "/android.jar")
            // JAVA HOME
            val javaBase = System.getProperty("java.home")
            var javaRt = "/lib/rt.jar"
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                if (!File(javaBase + javaRt).exists()) {
                    javaRt = "/../Classes/classes.jar"
                }
            }
            buildProguardJar.libraryjars(javaBase + "/" + javaRt)
            //混淆配置文件
            buildProguardJar.configuration(jarExtension.proguardConfigFile)
//            if (jarExtension.needDefaultProguard) {
//                buildProguardJar.configuration(android.getDefaultProguardFile("proguard-android.txt"))
//            }
            //applymapping
            val applyMappingFile = jarExtension.applyMappingFile
            buildProguardJar.applymapping(applyMappingFile)
            //输出mapping文件
            buildProguardJar.printmapping(jarExtension.outputFileDir + "/" + "mapping.txt")
//                    val buildJarBeforeDexTask = project.tasks.getByName()
            buildJarBeforeDexTask.dependsOn(dexTask.taskDependencies.getDependencies(dexTask))
            buildJar.dependsOn(buildJarBeforeDexTask)
            buildJar.doFirst {

            }
        }
//            }

//        }

    }

    private fun shouldFromJar(path: String): Boolean {
        return excludeJar.isEmpty()||!excludeJar.any { path.endsWith(it) }
    }

//    private fun shouldFromClass(path: String): Boolean {
//        return (excludePackage.isEmpty()||!excludePackage.any { path.contains(it) })
//                && (includePackage.isEmpty()||includePackage.any { path.contains(it) })
//                && (excludeClass.isEmpty()||!excludeClass.any { path.endsWith(it) })
//    }

}