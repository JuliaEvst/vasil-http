package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.asc
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import ru.ac.uniyar.domain.storage.Producer
import ru.ac.uniyar.domain.tables.ProducerTable

class ListProducerOperation(private val database: Database) {
    fun list(): List<Producer> =
        database
            .from(ProducerTable)
            .select()
            .orderBy(ProducerTable.time.asc(), ProducerTable.producerName.asc())
            .mapNotNull { row ->
                try {
                    Producer(
                        row[ProducerTable.producerName]!!,
                        row[ProducerTable.time]!!
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }

    fun subList(left: Int, pageSize: Int): List<Producer> =
        database
            .from(ProducerTable)
            .select()
            .orderBy(ProducerTable.time.asc(), ProducerTable.producerName.asc())
            .mapNotNull { row ->
                try {
                    Producer(
                        row[ProducerTable.producerName]!!,
                        row[ProducerTable.time]!!
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }
            .let {
                if (it.isNotEmpty()) {
                    if (left * pageSize - 1 <= it.size - 1) {
                        return@let it.subList((left - 1) * pageSize, left * pageSize)
                    }
                    if (left * pageSize - 2 <= it.size - 1) {
                        return@let it.subList((left - 1) * pageSize, left * pageSize - 1)
                    } else {
                        return@let it.subList((left - 1) * pageSize, left * pageSize - 2)
                    }
                } else {
                    return@let emptyList()
                }
            }

    fun count(): Double {
        return database
            .from(ProducerTable)
            .select(org.ktorm.dsl.count())
            .map { row -> row.getInt(1) }
            .first()
            .toDouble()
    }
}
