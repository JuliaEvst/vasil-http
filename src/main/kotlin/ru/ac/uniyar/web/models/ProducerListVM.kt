package ru.ac.uniyar.web.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.Producer

data class ProducerListVM(
    val producerList: List<Producer>,
    val path: String,
    val page: Int,
    val lastPage: Int
) : ViewModel
