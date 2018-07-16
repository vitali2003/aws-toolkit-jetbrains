package software.aws.toolkits.core.credentials

import org.slf4j.LoggerFactory

class ToolkitCredentialsProviderManager(
    private val registry: ToolkitCredentialsProviderRegistry
) {
    @Throws(CredentialProviderNotFound::class)
    fun getCredentialProvider(id: String): ToolkitCredentialsProvider {
        return registry.listFactories().mapNotNull { it.get(id) }.firstOrNull()
            ?: throw CredentialProviderNotFound("No ToolkitCredentialsProvider found represented by $id")
    }

    fun getCredentialProviders(): List<ToolkitCredentialsProvider> {
        return registry.listFactories().flatMap { it.listCredentialProviders() }.toList()
    }

    /**
     * Shuts down the manager and all registered factories
     */
    fun shutDown() {
        registry.listFactories().forEach {
            // TODO: Port over LOG.tryOrNull extension methods
            try {
                it.shutDown()
            } catch (e: Exception) {
                LOG.warn("ToolkitCredentialsProviderFactory '${it::class.qualifiedName}' threw exception when shutting down", e)
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ToolkitCredentialsProviderManager::class.java)
    }
}

class CredentialProviderNotFound(msg: String) : RuntimeException(msg)

/**
 * Registry of all possible [ToolkitCredentialsProviderFactory]
 */
interface ToolkitCredentialsProviderRegistry {
    fun listFactories(): Collection<ToolkitCredentialsProviderFactory>
}