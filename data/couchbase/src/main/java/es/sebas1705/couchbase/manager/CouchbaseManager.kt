package es.sebas1705.couchbase.manager

import com.couchbase.lite.Collection
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.ListenerToken
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import es.sebas1705.analytics.datasources.LogEventDataSource
import es.sebas1705.common.managers.ClassLogData
import es.sebas1705.common.utlis.extensions.types.logD
import es.sebas1705.common.utlis.extensions.types.logE
import es.sebas1705.couchbase.config.alias.DocMap
import javax.inject.Inject

/**
 * CouchbaseManager is responsible for managing Couchbase database operations.
 * It provides methods to select, insert, replace, delete documents, and register/unregister listeners.
 *
 * @property database [Database] The Couchbase database instance.
 * @property logEventDataSource [LogEventDataSource] Used for logging events.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
internal class CouchbaseManager @Inject constructor(
    private val database: Database?,
    private val logEventDataSource: LogEventDataSource
) : ClassLogData(), ICouchbaseManager {

    init {
        logAction("init databaseAvailable=${database != null}")
        if (database == null) {
            logError("Couchbase disabled: native database could not be initialized on this device/build")
        }
    }

    // Properties:
    private var collectionsTokens: MutableMap<String, ListenerToken?> = HashMap()
    private val documentsTokens: MutableMap<String, ListenerToken?> = HashMap()

    /**
     * Selects:
     */

    override fun count(
        collectionName: String
    ): Long = try {
        logAction("count start collection=$collectionName")
        if (database == null) {
            logAction("count skipped collection=$collectionName reason=db_unavailable")
            logResult("count", 0L)
            return 0L
        }
        val count = getAndCreateIfNotExist(collectionName).count
        logResult("count", count)
        count
    } catch (e: Exception) {
        logError("Error counting documents in $collectionName: ${e.message}")
        -1L
    }

    override fun countByParam(
        collectionName: String,
        param: String,
        vararg values: Any?
    ): Long = try {
        logAction("countByParam start collection=$collectionName param=$param")
        if (database == null) {
            logAction("countByParam skipped collection=$collectionName reason=db_unavailable")
            logResult("countByParam", 0L)
            return 0L
        }
        val queryValues = values
            .asSequence()
            .flatMap { value ->
                when (value) {
                    is Iterable<*> -> value.asSequence()
                    is Array<*> -> value.asSequence()
                    else -> sequenceOf(value)
                }
            }
            .toList()

        if (queryValues.isEmpty()) {
            logAction("countByParam skipped collection=$collectionName reason=empty_values")
            logResult("countByParam", 0L)
            return 0L
        }

        val collection = getAndCreateIfNotExist(collectionName)
        val whereExpression = queryValues
            .map { value -> Expression.property(param).equalTo(Expression.value(value)) }
            .reduce { acc, expression -> acc.or(expression) }

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
            .where(whereExpression)
        val result = query.execute()
        val count = result.count().toLong()
        logResult("countByParam", count)
        count
    } catch (e: Exception) {
        logError("Error counting documents by param in $collectionName: ${e.message}")
        -1L
    }

    override fun getAll(
        collectionName: String,
    ): List<DocMap> = try {
        logAction("getAll start collection=$collectionName")
        if (database == null) {
            logAction("getAll skipped collection=$collectionName reason=db_unavailable")
            logResult("getAll", emptyList<DocMap>())
            return emptyList()
        }
        val collection = getAndCreateIfNotExist(collectionName)
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
        val result = query.execute()
        val docs = result.allResults().mapNotNull { it.getDictionary(collectionName)?.toMap() }
        logResult("getAll", docs)
        docs
    } catch (e: Exception) {
        logError("Error getting all in $collectionName: ${e.message}")
        emptyList()
    }

    override fun get(
        collectionName: String,
        id: String
    ): DocMap? = try {
        logAction("get start collection=$collectionName id=$id")
        if (database == null) {
            logAction("get skipped collection=$collectionName id=$id reason=db_unavailable")
            logResult("get", null)
            return null
        }
        val document = database
            .getCollection(collectionName)
            ?.getDocument(id)
            ?.toMap()
        logResult("get", document)
        document
    } catch (e: Exception) {
        logError("Error getting doc in $collectionName by id $id: ${e.message}")
        null
    }

    override fun getByParam(
        collectionName: String,
        param: String,
        vararg values: Any?
    ): List<DocMap> = try {
        logAction("getByParam start collection=$collectionName param=$param")
        if (database == null) {
            logAction("getByParam skipped collection=$collectionName reason=db_unavailable")
            logResult("getByParam", emptyList<DocMap>())
            return emptyList()
        }
        val queryValues = values
            .asSequence()
            .flatMap { value ->
                when (value) {
                    is Iterable<*> -> value.asSequence()
                    is Array<*> -> value.asSequence()
                    else -> sequenceOf(value)
                }
            }
            .toList()

        if (queryValues.isEmpty()) {
            logAction("getByParam skipped collection=$collectionName reason=empty_values")
            logResult("getByParam", emptyList<DocMap>())
            return emptyList()
        }

        val collection = getAndCreateIfNotExist(collectionName)
        val whereExpression = queryValues
            .map { value -> Expression.property(param).equalTo(Expression.value(value)) }
            .reduce { acc, expression -> acc.or(expression) }

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
            .where(whereExpression)
        val result = query.execute()
        val docs = result.allResults().mapNotNull {
            it.getDictionary(collectionName)?.toMap()
        }
        logResult("getByParam", docs)
        docs
    } catch (e: Exception) {
        logError("Error getting by param in $collectionName: ${e.message}")
        emptyList()
    }

    /**
     * Upserts:
     */

    override fun upsert(
        collectionName: String,
        document: DocMap,
        id: String
    ): Boolean = try {
        logAction("upsert start collection=$collectionName id=$id")
        if (database == null) {
            logAction("upsert skipped collection=$collectionName id=$id reason=db_unavailable")
            logResult("upsert", false)
            false
        } else {
            val collection = getAndCreateIfNotExist(collectionName)
            collection.save(MutableDocument(id, document))
            logResult("upsert", true)
            true
        }
    } catch (e: Exception) {
        logError("Error upserting in collection $collectionName: ${e.message}")
        false
    }

    override fun upsertAll(
        collectionName: String,
        documents: List<DocMap>,
        ids: List<String>
    ) = try {
        logAction("upsertAll start collection=$collectionName count=${documents.size}")
        if (database == null) {
            logAction("upsertAll skipped collection=$collectionName reason=db_unavailable")
            logResult("upsertAll", false)
            false
        } else {
            val collection = getAndCreateIfNotExist(collectionName)
            documents.forEachIndexed { index, document ->
                collection.save(MutableDocument(ids[index], document))
            }
            logResult("upsertAll", true)
            true
        }
    } catch (e: Exception) {
        logError("Error upserting all in collection $collectionName: ${e.message}")
        false
    }

    /**
     * Deletes:
     */

    override fun delete(
        collectionName: String,
        id: String
    ): Boolean = try {
        logAction("delete start collection=$collectionName id=$id")
        if (database == null) {
            logAction("delete skipped collection=$collectionName id=$id reason=db_unavailable")
            logResult("delete", false)
            false
        } else {
            val collection = getAndCreateIfNotExist(collectionName)
            collection.purge(id)
            logResult("delete", true)
            true
        }
    } catch (e: Exception) {
        logError("Error deleting in collection $collectionName: ${e.message}")
        false
    }

    override fun deleteAll(
        collectionName: String,
        ids: List<String>
    ): Boolean {
        logAction("deleteAllByIds start collection=$collectionName count=${ids.size}")
        return try {
            if (database == null) {
                logAction("deleteAllByIds skipped collection=$collectionName reason=db_unavailable")
                logResult("deleteAllByIds", false)
                return false
            }
            val collection = getAndCreateIfNotExist(collectionName)
            ids.forEach(collection::purge)
            logResult("deleteAllByIds", true)
            true
        } catch (e: Exception) {
            logError("Error deleting all in collection $collectionName: ${e.message}")
            false
        }
    }

    override fun deleteAll(
        collectionName: String
    ): Boolean {
        logAction("deleteAll start collection=$collectionName")
        return try {
            if (database == null) {
                logAction("deleteAll skipped collection=$collectionName reason=db_unavailable")
                logResult("deleteAll", false)
                return false
            }
            val collection = getAndCreateIfNotExist(collectionName)
            // The document id is read from the metadata: a `SelectResult.all()` row is keyed by collection name, so the
            // previous lookup of a top level field never found an id and nothing was purged.
            val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.collection(collection))
            query.execute().allResults().mapNotNull { it.getString(0) }.forEach(collection::purge)
            logResult("deleteAll", true)
            true
        } catch (e: Exception) {
            logError("Error deleting all in collection $collectionName: ${e.message}")
            false
        }
    }

    override fun deleteByParam(
        collectionName: String,
        param: String,
        value: Any
    ): Boolean {
        logAction("deleteByParam start collection=$collectionName param=$param")
        return try {
            if (database == null) {
                logAction("deleteByParam skipped collection=$collectionName reason=db_unavailable")
                logResult("deleteByParam", false)
                return false
            }
            val collection = getAndCreateIfNotExist(collectionName)
            val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.collection(collection))
                .where(Expression.property(param).equalTo(Expression.value(value)))
            query.execute().allResults().mapNotNull { it.getString(0) }.forEach(collection::purge)
            logResult("deleteByParam", true)
            true
        } catch (e: Exception) {
            logError("Error deleting all in collection $collectionName: ${e.message}")
            false
        }
    }

    /**
     * Register Listeners:
     */

    override fun registerCollectionChangeListener(
        collectionName: String,
        listener: (documents: List<DocMap>) -> Unit,
        retrieveOnlyChanges: Boolean
    ) {
        logAction("registerCollectionListener start collection=$collectionName onlyChanges=$retrieveOnlyChanges")
        try {
            if (database == null) {
                logAction("registerCollectionListener skipped collection=$collectionName reason=db_unavailable")
                return
            }

            val collection = database.getCollection(collectionName)
            collection?.let {
                val token = it.addChangeListener { changes ->
                    if (retrieveOnlyChanges) {
                        val changedDocs = changes.documentIDs.mapNotNull { docIds ->
                            it.getDocument(docIds)?.toMap()
                        }
                        logResult("registerCollectionListener.callback", changedDocs)
                        listener(changedDocs)
                    } else {
                        val allDocs = try {
                            QueryBuilder.select(SelectResult.all())
                                .from(DataSource.collection(collection))
                                .execute()
                                .allResults()
                                .mapNotNull { it.getDictionary(collectionName)?.toMap() }
                        } catch (e: Exception) {
                            logError("registerCollectionListener.callback query failed: ${e.message}")
                            emptyList()
                        }
                        logResult("registerCollectionListener.callback", allDocs)
                        listener(allDocs)
                    }
                }
                collectionsTokens[collectionName] = token
                logResult("registerCollectionListener", true)
            } ?: run {
                logError("Error registering collection change listener in collection $collectionName: Collection not found")
                logResult("registerCollectionListener", false)
            }
        } catch (e: Exception) {
            logError("Error registering collection change listener in collection $collectionName: ${e.message}")
        }
    }

    override fun registerDocumentChangeListener(
        collectionName: String,
        documentId: String,
        listener: (changedDocument: DocMap) -> Unit
    ) {
        logAction("registerDocumentListener start collection=$collectionName id=$documentId")
        try {
            if (database == null) {
                logAction("registerDocumentListener skipped collection=$collectionName id=$documentId reason=db_unavailable")
                return
            }

            database.getCollection(collectionName)?.let { collection ->
                val token = collection.addDocumentChangeListener(documentId) { change ->
                    collection.getDocument(change.documentID)?.let { doc ->
                        val changedDocument = doc.toMap()
                        logResult("registerDocumentListener.callback", changedDocument)
                        listener(changedDocument)
                    } ?: run {
                        logError("Error registering document change listener in collection $collectionName: Document not found")
                    }
                }
                documentsTokens["$collectionName-$documentId"] = token
                logResult("registerDocumentListener", true)
            } ?: run {
                logError("Error registering document change listener in collection $collectionName: Collection not found")
                logResult("registerDocumentListener", false)
            }
        } catch (e: Exception) {
            logError("Error registering document change listener in collection $collectionName: ${e.message}")
        }
    }

    /**
     * Unregister Listeners:
     */

    override fun unregisterCollectionChangeListener(
        collectionName: String
    ) {
        logAction("unregisterCollectionListener start collection=$collectionName")
        try {
            collectionsTokens[collectionName]?.let { token ->
                token.remove()
                collectionsTokens.remove(collectionName)
                logResult("unregisterCollectionListener", true)
            } ?: run {
                logError("Error unregistering collection change listener in collection $collectionName: Listener not found")
                logResult("unregisterCollectionListener", false)
            }
        } catch (e: Exception) {
            logError("Error unregistering collection change listener in collection $collectionName: ${e.message}")
        }
    }

    override fun unregisterDocumentChangeListener(
        collectionName: String,
        documentId: String
    ) {
        logAction("unregisterDocumentListener start collection=$collectionName id=$documentId")
        try {
            documentsTokens["$collectionName-$documentId"]?.let { token ->
                token.remove()
                documentsTokens.remove("$collectionName-$documentId")
                logResult("unregisterDocumentListener", true)
            } ?: run {
                logError("Error unregistering document change listener in collection $collectionName: Listener not found")
                logResult("unregisterDocumentListener", false)
            }
        } catch (e: Exception) {
            logError("Error unregistering document change listener in collection $collectionName: ${e.message}")
        }
    }

    //Privates:

    private fun logAction(message: String) {
        logD("couchbase.action $message")
    }

    private fun logResult(action: String, value: Any?) {
        logD("couchbase.result action=$action value=${summarizeResult(value)}")
    }

    private fun summarizeResult(value: Any?): String {
        return when (value) {
            null -> "null"
            is kotlin.collections.Collection<*> -> if (value.size > LARGE_COLLECTION_THRESHOLD) {
                "Collection(size=${value.size})"
            } else {
                compactText(value.toString())
            }
            is Map<*, *> -> if (value.size > LARGE_MAP_THRESHOLD) {
                "Map(size=${value.size})"
            } else {
                compactText(value.toString())
            }
            is Array<*> -> if (value.size > LARGE_ARRAY_THRESHOLD) {
                "Array(size=${value.size})"
            } else {
                compactText(value.contentToString())
            }
            is String -> if (value.length > LARGE_TEXT_THRESHOLD) {
                "String(length=${value.length})"
            } else {
                value
            }
            else -> {
                val text = value.toString()
                if (text.length > LARGE_TEXT_THRESHOLD) {
                    "${value::class.java.simpleName}(length=${text.length})"
                } else {
                    compactText(text)
                }
            }
        }
    }

    private fun compactText(text: String): String {
        if (text.length <= LARGE_TEXT_THRESHOLD) return text
        return "${text.take(LARGE_TEXT_THRESHOLD)}...(length=${text.length})"
    }

    /**
     * Logs an error message and sends it to the log event data source.
     *
     * @param message [String] The error message to log.
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    private fun logError(message: String) {
        logE("couchbase.error $message")
        logEventDataSource.logError(this, message)
    }

    /**
     * Gets or creates a collection if it does not exist.
     *
     * @param collectionName [String] The name of the collection to retrieve or create.
     *
     * @return [Collection]? The collection if it exists or was created, null otherwise.
     *
     * @since 0.1.0
     * @author Sebas1705 24/07/2025
     */
    private fun getAndCreateIfNotExist(
        collectionName: String
    ): Collection =
        database?.getCollection(collectionName) ?: database!!.createCollection(collectionName)

    private companion object {
        private const val LARGE_TEXT_THRESHOLD = 320
        private const val LARGE_COLLECTION_THRESHOLD = 20
        private const val LARGE_MAP_THRESHOLD = 30
        private const val LARGE_ARRAY_THRESHOLD = 20
    }
}