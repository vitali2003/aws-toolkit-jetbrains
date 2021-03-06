// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.core.credentials

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.util.Disposer
import software.aws.toolkits.core.credentials.CredentialProviderNotFound
import software.aws.toolkits.core.credentials.ToolkitCredentialsProvider
import software.aws.toolkits.core.credentials.ToolkitCredentialsProviderManager

interface CredentialManager {
    @Throws(CredentialProviderNotFound::class)
    fun getCredentialProvider(providerId: String): ToolkitCredentialsProvider

    fun getCredentialProviders(): List<ToolkitCredentialsProvider>

    companion object {
        fun getInstance(): CredentialManager = ServiceManager.getService(CredentialManager::class.java)
    }
}

class DefaultCredentialManager : CredentialManager, Disposable {
    private val toolkitCredentialManager =
        ToolkitCredentialsProviderManager(ExtensionPointCredentialsProviderRegistry())

    init {
        Disposer.register(ApplicationManager.getApplication(), this)
    }

    @Throws(CredentialProviderNotFound::class)
    override fun getCredentialProvider(providerId: String): ToolkitCredentialsProvider = toolkitCredentialManager.getCredentialProvider(providerId)

    override fun getCredentialProviders(): List<ToolkitCredentialsProvider> = toolkitCredentialManager.getCredentialProviders()

    override fun dispose() {
        toolkitCredentialManager.shutDown()
    }
}