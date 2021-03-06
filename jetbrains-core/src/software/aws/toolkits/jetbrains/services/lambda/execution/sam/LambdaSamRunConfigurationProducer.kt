// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.lambda.execution.sam

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLPsiElement
import software.aws.toolkits.jetbrains.services.lambda.LambdaHandlerResolver
import software.aws.toolkits.jetbrains.services.lambda.RuntimeGroup
import software.aws.toolkits.jetbrains.services.lambda.execution.LambdaRunConfiguration
import software.aws.toolkits.jetbrains.services.lambda.runtimeGroup

class LambdaSamRunConfigurationProducer : RunConfigurationProducer<SamRunConfiguration>(getFactory()) {
    // Filter all Lambda run CONFIGURATIONS down to only ones that are Lambda SAM for this run producer
    override fun getConfigurationSettingsList(runManager: RunManager): List<RunnerAndConfigurationSettings> =
        super.getConfigurationSettingsList(runManager).filter { it.configuration is SamRunConfiguration }

    override fun setupConfigurationFromContext(configuration: SamRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>): Boolean {
        val element = context.psiLocation ?: return false
        val parent = element.parent
        val result = when (parent) {
            is YAMLKeyValue -> setupFromTemplate(parent, configuration)
            else -> setupFromSourceFile(element, context, configuration)
        }
        if (result) {
            configuration.setGeneratedName()
        }
        return result
    }

    override fun isConfigurationFromContext(configuration: SamRunConfiguration, context: ConfigurationContext): Boolean {
        val element = context.psiLocation ?: return false
        val parent = element.parent
        return when (parent) {
            is YAMLPsiElement -> isFromTemplateContext(parent, configuration)
            else -> isFromSourceFileContext(element, configuration)
        }
    }

    private fun setupFromSourceFile(element: PsiElement, context: ConfigurationContext, configuration: SamRunConfiguration): Boolean {
        val runtimeGroup = element.language.runtimeGroup ?: return false
        if (runtimeGroup !in LambdaHandlerResolver.supportedRuntimeGroups) {
            return false
        }
        val resolver = LambdaHandlerResolver.getInstanceOrThrow(runtimeGroup)
        val handler = resolver.determineHandler(element) ?: return false

        val sdk = ModuleRootManager.getInstance(context.module).sdk ?: ProjectRootManager.getInstance(context.project).projectSdk

        val runtime = sdk?.let { RuntimeGroup.runtimeForSdk(it) }
        configuration.configure(runtime, handler)
        return true
    }

    private fun setupFromTemplate(element: YAMLPsiElement, configuration: SamRunConfiguration): Boolean {
        val file = element.containingFile?.virtualFile?.path ?: return false
        val function = functionFromElement(element) ?: return false
        configuration.configure(templateFile = file, logicalFunctionName = function.logicalName)
        return true
    }

    private fun isFromSourceFileContext(element: PsiElement, configuration: SamRunConfiguration): Boolean {
        val runtimeGroup = element.language.runtimeGroup ?: return false
        if (runtimeGroup !in LambdaHandlerResolver.supportedRuntimeGroups) {
            return false
        }
        val resolver = LambdaHandlerResolver.getInstanceOrThrow(runtimeGroup)
        val handler = resolver.determineHandler(element) ?: return false
        return configuration.settings.handler == handler
    }

    private fun isFromTemplateContext(element: YAMLPsiElement, configuration: SamRunConfiguration): Boolean {
        val templateFile = configuration.settings.templateFile ?: return false
        val functionName = configuration.settings.logicalFunctionName ?: return false
        val file = element.containingFile?.virtualFile?.path ?: return false
        val function = functionFromElement(element) ?: return false
        return templateFile == file && functionName == function.logicalName
    }

    companion object {
        private fun getFactory() = LambdaRunConfiguration.getInstance().configurationFactories.first { it is SamRunConfigurationFactory }
    }
}