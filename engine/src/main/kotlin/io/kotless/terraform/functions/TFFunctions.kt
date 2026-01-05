package io.kotless.terraform.functions

import io.terraformkt.utils.toText
import java.io.File

//Escaping required for Windows
private fun escape(value: String) = value.replace("\\", "\\\\")

/** Get a canonical path of file */
fun path(file: File): String = escape(file.canonicalPath)

fun filemd5(file: File) = filemd5(path(file))
fun filemd5(file: String) = "filemd5(${toText(file)})"

fun filesha256(file: String) = "filesha256(${toText(file)})"

fun file(file: File) = file(path(file))
fun file(file: String) = "file(${toText(file)})"

fun timestamp() = "timestamp()"

fun link(field: String) = "\${$field}"

fun eval(func: String) = "\${$func}"
