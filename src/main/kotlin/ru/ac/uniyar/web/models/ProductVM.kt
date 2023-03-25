package ru.ac.uniyar.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel

data class ProductVM(
    val id: Int,
    val time: String,
    val category: String,
    val name: String,
    val description: String,
    val price: Int,
    val count: Int,
    val producer: String,
    val country: String,
    val parentCategory: String,
    val form: WebForm = WebForm()
) : ViewModel
