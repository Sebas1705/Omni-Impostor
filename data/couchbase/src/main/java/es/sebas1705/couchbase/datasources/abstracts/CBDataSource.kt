package es.sebas1705.couchbase.datasources.abstracts

import es.sebas1705.couchbase.documents.abstracts.Document
import es.sebas1705.couchbase.documents.abstracts.fromMap
import es.sebas1705.couchbase.manager.ICouchbaseManager
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

abstract class CBDataSource <T : Document> (
    private val couchbaseManager: ICouchbaseManager,
    private val clazz: KClass<T>,
    collectionNameOverride: String? = null
) {

    private val collectionName: String =
        collectionNameOverride ?: (clazz.simpleName!! + "_collection")

    fun count() = couchbaseManager.count(collectionName)

    fun get(id: String) = couchbaseManager
        .get(collectionName, id)
        ?.fromMap(clazz)

    fun getAll() = couchbaseManager
        .getAll(collectionName)
        .map { it.fromMap(clazz) }

    fun <R> getByParam(param: KProperty1<T, R>, vararg values: Any) = couchbaseManager
        .getByParam(collectionName, param.name, *values)
        .map { it.fromMap(clazz) }

    fun <R> getFirstByParam(param: KProperty1<T, R>, vararg values: Any) =
        couchbaseManager.getByParam(collectionName, param.name, *values).firstOrNull()?.fromMap(clazz)

    fun upsert(resource: T) = couchbaseManager
        .upsert(collectionName, resource.asMap(), resource.idUnique!!)

    fun upsertAll(resources: List<T>) = couchbaseManager
        .upsertAll(collectionName, resources.map { it.asMap() }, resources.mapNotNull { it.idUnique })

    fun delete(id: String) = couchbaseManager.delete(collectionName, id)

    fun deleteAll(ids: List<String>) = couchbaseManager.deleteAll(collectionName, ids)

    fun deleteAll() = couchbaseManager.deleteAll(collectionName)

    fun <R> deleteByParam(param: KProperty1<T, R>, value: String) =
        couchbaseManager.deleteByParam(collectionName, param.name, value)

    fun setCollectionAllListener(
        onChange: (all: List<T>) -> Unit,
    ) = couchbaseManager.registerCollectionChangeListener(
        collectionName,
        listener = { changes ->
            val all = changes.map { it.fromMap(clazz) }
            onChange(all)
        },
        retrieveOnlyChanges = false
    )

    fun setCollectionPartialListener(
        onChange: (changedItems: List<T>) -> Unit,
    ) = couchbaseManager.registerCollectionChangeListener(
        collectionName,
        listener = { changes ->
            val all = changes.map { it.fromMap(clazz) }
            onChange(all)
        },
        retrieveOnlyChanges = true
    )

    fun removeCollectionListener() = couchbaseManager.unregisterCollectionChangeListener(collectionName)

    fun setDocumentListener(
        id: String,
        onChange: (T) -> Unit,
    ) = couchbaseManager.registerDocumentChangeListener(
        collectionName,
        id,
        listener = { change ->
            val document = change.fromMap(clazz)
            onChange(document)
        }
    )

    fun removeDocumentListener(id: String) = couchbaseManager.unregisterDocumentChangeListener(collectionName, id)
}