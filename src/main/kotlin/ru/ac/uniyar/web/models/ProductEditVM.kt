package ru.ac.uniyar.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.Product

data class ProductEditVM(
    val product: Product,
    val form: WebForm = WebForm()
) : ViewModel
