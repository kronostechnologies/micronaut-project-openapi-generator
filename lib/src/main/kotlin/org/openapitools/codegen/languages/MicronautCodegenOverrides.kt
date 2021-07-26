package org.openapitools.codegen.languages

import java.io.File

data class OutputFolders(
    val project: String,
    val source: String = "$project/java",
)

open class MicronautCodegenOverrides(
    protected val sourceFolders: OutputFolders,
    protected val testFolders: OutputFolders
) : MicronautCodegen() {
    private val generatedSourceFolder: String = "generated-sources" + File.separator + "openapi"
    private val generatedTestSourceFolder: String = "generated-test-sources" + File.separator + "openapi"

    override fun processOpts() {
        super.processOpts()

        projectFolder = sourceFolders.project
        sourceFolder = sourceFolders.source
        projectTestFolder = testFolders.project
        testFolder = testFolders.source
    }

    /**
     * Paths are overridden in the middle of `super.processOpts()` with `addSupportingFile()` called right after.
     * We don't have any opportunities to set our paths in between, so we need to monkey-patch this method.
     */
    override fun addSupportingFile(folder: String?, packageString: String?, file: String?) {
        val projectFolder = when (folder) {
            generatedSourceFolder -> sourceFolders.source
            generatedTestSourceFolder -> testFolders.source
            else -> folder
        }
        super.addSupportingFile(projectFolder, packageString, file)
    }
}
