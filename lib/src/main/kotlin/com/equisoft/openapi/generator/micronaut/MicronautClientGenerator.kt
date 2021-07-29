package com.equisoft.openapi.generator.micronaut

import io.micronaut.core.annotation.NonNull
import io.micronaut.core.annotation.Nullable
import org.apache.commons.io.FilenameUtils
import org.openapitools.codegen.CodegenType
import org.openapitools.codegen.CodegenType.CLIENT
import org.openapitools.codegen.SupportingFile
import org.openapitools.codegen.languages.MicronautCodegenOverrides
import org.openapitools.codegen.languages.OutputFolders
import java.io.File
import java.nio.file.Files.getPosixFilePermissions
import java.nio.file.Files.setPosixFilePermissions
import java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE

private const val GRADLE_WRAPPER_FOLDER = "gradle/wrapper"

open class MicronautClientGenerator : MicronautCodegenOverrides(
    sourceFolders = OutputFolders("src/main"),
    testFolders = OutputFolders("src/test")
) {
    init {
        typeMapping["Nullable"] = Nullable::class.java.name
        typeMapping["Nonnull"] = NonNull::class.java.name
    }

    override fun getName(): String = "micronaut"
    override fun getTag(): CodegenType = CLIENT

    override fun processOpts() {
        enablePostProcessFile = true

        super.processOpts()

        populateSupportingFiles()
    }

    private fun populateSupportingFiles() {
        addProjectFile("README.mustache", "README.md")
        addProjectFile("gitignore.mustache", ".gitignore").doNotOverwrite()
        addProjectFile("tool-versions.mustache", ".tool-versions").doNotOverwrite()

        addProjectFile("build.gradle.kts.mustache", "build.gradle.kts")
        addProjectFile("settings.gradle.kts.mustache", "settings.gradle.kts")

        addProjectFile("gradlew", "gradlew")
        addProjectFile("gradlew.bat", "gradlew.bat")
        addProjectFile("gradle-wrapper.properties", "gradle-wrapper.properties", GRADLE_WRAPPER_FOLDER)
        addProjectFile("gradle-wrapper.jar", "gradle-wrapper.jar", GRADLE_WRAPPER_FOLDER)

        addSourceFile(invokerPackage, "Client")
        addSourceFile(invokerPackage, "NonNullApi")
        addSourceFile(invokerPackage, "NonNullFields")
        addSourceFile(apiPackage, "package-info")
    }

    protected open fun addProjectFile(
        templateFile: String,
        targetFile: String,
        folder: String = ""
    ): SupportingFile {
        val supportingFile = SupportingFile("project/$templateFile", folder, targetFile)

        addUniqueSupportingFile(supportingFile)

        return supportingFile
    }

    protected open fun addSourceFile(
        packageName: String,
        fileName: String,
        folder: OutputFolders = sourceFolders
    ): SupportingFile {
        val supportingFile = SupportingFile(
            "source/$fileName.mustache",
            "${folder.source}/${packageName.replace(".", "/")}",
            "$fileName.java"
        )

        addUniqueSupportingFile(supportingFile)

        return supportingFile
    }

    private fun addUniqueSupportingFile(supportingFile: SupportingFile) {
        if (!supportingFiles.contains(supportingFile)) {
            supportingFiles.add(supportingFile)
        }
    }

    override fun postProcessFile(file: File?, fileType: String?) {
        super.postProcessFile(file, fileType)

        if (file != null && FilenameUtils.getName(file.toString()) == "gradlew") {
            val path = file.toPath()
            setPosixFilePermissions(path, getPosixFilePermissions(path) + mutableSetOf(
                OWNER_EXECUTE,
                GROUP_EXECUTE,
                OTHERS_EXECUTE
            ))
        }
    }
}
