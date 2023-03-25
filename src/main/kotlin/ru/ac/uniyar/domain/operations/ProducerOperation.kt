package ru.ac.uniyar.domain.operations

import org.http4k.lens.WebForm
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.tables.ProducerTable
import ru.ac.uniyar.web.handlers.producerNameField
import java.text.SimpleDateFormat
import java.util.Date

class ProducerOperation(private val database: Database) {
    fun isNewProducer(webForm: WebForm): Boolean {
        val producerName = producerNameField(webForm)
        var isNew: Boolean = false
        database
            .from(ProducerTable)
            .select()
            .where { ProducerTable.producerName eq producerName }
            .map { isNew = true }
        return isNew
    }

    fun add(webForm: WebForm) {
        database.insert(ProducerTable) {
            set(it.producerName, producerNameField(webForm))
            set(it.time, SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date()))
        }
    }
}
