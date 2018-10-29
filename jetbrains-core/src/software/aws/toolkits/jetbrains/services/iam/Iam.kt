// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.iam

import software.amazon.awssdk.services.iam.IamClient
import software.amazon.awssdk.services.iam.model.Role
import kotlin.streams.asSequence

fun IamClient.listRolesFilter(predicate: (Role) -> Boolean): Sequence<Role> = this.listRolesPaginator().roles().stream().asSequence().filter(predicate)

data class IamRole(val name: String, val arn: String) {
    override fun toString(): String = name

    companion object {
        private val ARN_REGEX = Regex("arn:.+:iam::.+:role/(.+)")
        fun fromArn(arn: String): IamRole {
            val name = ARN_REGEX.matchEntire(arn)?.groups?.elementAtOrNull(1)?.value ?: arn
            return IamRole(name, arn)
        }
    }
}