// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.utils

import com.nhaarman.mockitokotlin2.KStubbing
import com.nhaarman.mockitokotlin2.withSettings
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import software.amazon.awssdk.core.SdkRequest
import kotlin.reflect.full.isSubclassOf

/**
 * Mockito Answer that will delegate the default helper methods (such as the consumers) to the final method that takes
 * the SdkRequest
 */
class DelegateSdkConsumers : Answer<Any> {
    override fun answer(invocation: InvocationOnMock): Any? {
        val method = invocation.method
        return if (method.isDefault &&
            method?.parameters?.getOrNull(0)?.type?.kotlin?.isSubclassOf(SdkRequest::class) != true
        ) {
            invocation.callRealMethod()
        } else {
            Mockito.RETURNS_DEFAULTS.answer(invocation)
        }
    }
}

inline fun <reified T : Any> delegateMock(stubbing: KStubbing<T>.(T) -> Unit): T = Mockito.mock(
    T::class.java,
    withSettings(
        defaultAnswer = DelegateSdkConsumers()
    )
).apply {
    KStubbing(this).stubbing(this)
}
