package es.sebas1705.couchbase.documents.abstracts

import es.sebas1705.couchbase.config.alias.DocMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

/**
 * Document is an abstract class that provides a method to convert the document's properties
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
open class Document(
    val idUnique: String?
) {

    /**
     * Converts the document's properties to a map.
     *
     * @return A map where the keys are the property names and the values are the property values.
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    fun asMap(): DocMap {
        return this::class.memberProperties.associate { prop ->
            @Suppress("UNCHECKED_CAST")
            val typedProp = prop as kotlin.reflect.KProperty1<Any, *>
            typedProp.isAccessible = true
            val mappedValue = when (val value = typedProp.get(this)) {
                is Document -> value.asMap()
                is List<*> -> value.map { if (it is Document) it.asMap() else it }
                else -> value
            }
            typedProp.name to mappedValue
        }
    }
}

/**
 * Extension function to convert a [DocMap] to a document of type [T].
 *
 * @return An instance of [T] created from the map.
 * @throws IllegalArgumentException if the class does not have a primary constructor.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Document> DocMap.fromMap(): T {
    val ctor = T::class.primaryConstructor
        ?: throw IllegalArgumentException("No primary constructor for ${T::class}")
    val args = ctor.parameters.mapNotNull { param ->
        val value = this[param.name]
        if (value == null && param.isOptional) return@mapNotNull null
        val classifier = param.type.classifier
        val mapped = when {
            value is Long && classifier == Int::class -> value.toInt()
            value is Map<*, *> && classifier is KClass<*> && Document::class.java.isAssignableFrom(classifier.java) ->
                (value as Map<String, Any?>).fromMap(classifier as KClass<out Document>)
            else -> value
        }
        param to mapped
    }.toMap()
    return ctor.callBy(args)
}

/**
 * Extension function to convert a [Map] to a document of type [T].
 *
 * @param clazz The class of the document to create.
 * @return An instance of [T] created from the map.
 * @throws IllegalArgumentException if the class does not have a primary constructor.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : Document> DocMap.fromMap(clazz: KClass<T>): T {
    val ctor = clazz.primaryConstructor
        ?: throw IllegalArgumentException("No primary constructor for $clazz")
    val args = ctor.parameters.mapNotNull { param ->
        val value = this[param.name]
        if (value == null && param.isOptional) return@mapNotNull null
        val classifier = param.type.classifier
        val mapped = when {
            value is Long && classifier == Int::class -> value.toInt()
            value is Map<*, *> && classifier is KClass<*> && Document::class.java.isAssignableFrom(classifier.java) ->
                (value as Map<String, Any?>).fromMap(classifier as KClass<out Document>)
            else -> value
        }
        param to mapped
    }.toMap()
    return ctor.callBy(args)
}