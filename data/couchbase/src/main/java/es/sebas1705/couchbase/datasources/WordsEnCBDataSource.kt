package es.sebas1705.couchbase.datasources

import es.sebas1705.couchbase.datasources.abstracts.CBDataSource
import es.sebas1705.couchbase.documents.WordDoc
import es.sebas1705.couchbase.manager.ICouchbaseManager
import javax.inject.Inject

class WordsEnCBDataSource @Inject constructor(
    couchbaseManager: ICouchbaseManager
): CBDataSource<WordDoc>(couchbaseManager, WordDoc::class, "WordDoc_en_collection")

