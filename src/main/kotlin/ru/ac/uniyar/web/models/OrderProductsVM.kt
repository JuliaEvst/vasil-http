package ru.ac.uniyar.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.User

data class OrderProductsVM(
    val currentUser: User,
    val form: WebForm = WebForm()
) : ViewModel
