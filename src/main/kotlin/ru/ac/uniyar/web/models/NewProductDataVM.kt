package ru.ac.uniyar.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.Category
import ru.ac.uniyar.domain.storage.Producer

data class NewProductDataVM(
    val form: WebForm = WebForm(),
    val listProducer: List<Producer>,
    val listCategory: List<Category>
) : ViewModel
