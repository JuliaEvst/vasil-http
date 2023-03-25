package ru.ac.uniyar.web.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.ParentCategory

data class NewCategoryDataVM(
    val listParentCategory: List<ParentCategory>,
    val form: WebForm = WebForm()
) : ViewModel
