@file:Suppress("UnnecessaryVariable")

package academy.kt

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.Variance.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.toClassName
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class ProviderGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        return resolver
            .getSymbolsWithAnnotation(Provide::class.qualifiedName!!)
            .also { logger.warn("Processing " + it.joinToString { it.toString() }) }
            .filterIsInstance<KSClassDeclaration>()
            .filterNot { generateProvider(it, resolver) }.toList()
    }

    private fun generateProvider(classDeclaration: KSClassDeclaration, resolver: Resolver): Boolean {
        val className = classDeclaration.simpleName.getShortName()
        val providerName = className + "Provider"
        val constructorParameters = classDeclaration.getConstructors().firstOrNull()?.parameters.orEmpty()
        val propertySpecs = constructorParameters.mapNotNull {
            it.name ?: return@mapNotNull null
            val providerClassName = "provider." + it.type.resolve().toClassName().simpleName + "Provider"
            val providerDeclaration = resolver.getClassDeclarationByName(providerClassName)
                ?: return false
            val argumentProperyName = it.name!!.getShortName() + "Provider"
            PropertySpec.builder(argumentProperyName, providerDeclaration.toClassName())
                .initializer("${providerDeclaration.simpleName.getShortName()}()")
                .build()
        }
        val fileSpec = FileSpec
            .builder("provider", providerName)
            .addType(
                TypeSpec
                    .classBuilder(providerName)
                    .addProperties(
                        propertySpecs
                    )
                    .addFunction(
                        FunSpec
                            .builder("provide")
                            .returns(classDeclaration.toClassName())
                            .addCode("return $className(${propertySpecs.joinToString { "${it.name}.provide()" }})")
                            .build()
                    )
                    .build()
            )
            .build()

        val file = codeGenerator.createNewFile(fileSpec.kspDependencies(true), fileSpec.packageName, fileSpec.name)
        OutputStreamWriter(file, StandardCharsets.UTF_8)
            .use(fileSpec::writeTo)
        return true
    }
}