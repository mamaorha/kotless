package io.kotless.parser.utils.psi

import io.kotless.parser.utils.errors.withExceptionHeader
import org.jetbrains.kotlin.js.descriptorUtils.nameIfStandardType
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.TypedCompileTimeConstant
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator

fun KtValueArgument.asString(binding: BindingContext): String {
    val expr = this.getArgumentExpression()
    require(expr != null) { withExceptionHeader("argument is not an expression") }

    val value = ConstantExpressionEvaluator.getConstant(expr, binding)
    require(value is TypedCompileTimeConstant && value.type.nameIfStandardType?.identifier == "String") {
        withExceptionHeader("argument should be compile-time constant string")
    }

    return value.constantValue.value as String
}
