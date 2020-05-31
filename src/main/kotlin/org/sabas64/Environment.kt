package org.sabas64

class Environment<T> {
    val variables = mutableMapOf<String, T>()
    val functions = mutableMapOf<String, T>()
    val arrays = mutableMapOf<String, T>()
}
