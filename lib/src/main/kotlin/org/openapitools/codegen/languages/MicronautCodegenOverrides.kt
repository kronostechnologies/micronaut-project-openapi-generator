package org.openapitools.codegen.languages

import io.micronaut.core.util.CollectionUtils
import io.swagger.v3.oas.models.media.ComposedSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import org.openapitools.codegen.utils.ModelUtils
import org.slf4j.LoggerFactory
import java.io.File

data class OutputFolders(
    val project: String,
    val source: String = "$project/java",
)

@Suppress("VariableNaming")
open class MicronautCodegenOverrides(
    protected val sourceFolders: OutputFolders,
    protected val testFolders: OutputFolders
) : MicronautCodegen() {
    private val generatedSourceFolder: String = "generated-sources" + File.separator + "openapi"
    private val generatedTestSourceFolder: String = "generated-test-sources" + File.separator + "openapi"
    private val LOGGER = LoggerFactory.getLogger(AbstractJavaCodegen::class.java)

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

    @Suppress("UnsafeCallOnNullableType", "ReturnCount")
    override fun getSchemaType(schema: Schema<Any>): String? {
        val format = schema.format
        if (schema is StringSchema && format != null && CUSTOM_FORMATS.containsKey(format)) {
            val type = CUSTOM_FORMATS.get(format)!!.name
            LOGGER.warn("Use custom format {} with type {}.", format, type)
            return type
        }
        if (schema is ComposedSchema) {
            return getComposedSchematype(schema)
        }
        return super.getSchemaType(schema)
    }

    @Suppress("ReturnCount")
    private fun getComposedSchematype(schema: ComposedSchema): String? {
        if (CollectionUtils.isNotEmpty(schema.oneOf as Collection<*>?)) {
            if (schema.nullable && schema.oneOf.count() == 1) {
                val inner: Schema<Any>? = schema.oneOf
                    .stream()
                    .filter { subSchema ->
                        !ModelUtils.isNullType(subSchema)
                    }
                    .findFirst()
                    .orElse(null)
                if (inner != null) {
                    return super.getSchemaType(inner)
                }
            }

            // ignore embedded oneOf schemas
            return Any::class.java.name
        }
        return super.getSchemaType(schema)
    }
}
