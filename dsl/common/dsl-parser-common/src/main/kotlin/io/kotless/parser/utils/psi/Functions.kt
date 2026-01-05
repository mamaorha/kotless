package io.kotless.parser.utils.psi

import io.kotless.parser.utils.psi.visitor.KtDefaultVisitor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.parents

fun KtElement.visitNamedFunctions(filter: (KtNamedFunction) -> Boolean = { true }, body: (KtNamedFunction) -> Unit) {
    accept(object : KtDefaultVisitor() {
        override fun visitNamedFunction(function: KtNamedFunction) {
            if (filter(function)) body(function)

            super.visitNamedFunction(function)
        }
    })
}


/** Tell if this function `static` -- either top-level or in Kotlin Object */
fun KtNamedFunction.isStatic(): Boolean {
    return isTopLevel || (parents.firstOrNull { it is KtClassOrObject } is KtObjectDeclaration)
}
