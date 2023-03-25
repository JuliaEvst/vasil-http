package ru.ac.uniyar.web.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.Category
import ru.ac.uniyar.domain.storage.ParentCategory

data class StartPageVM(
    val categories: Map<ParentCategory, List<Category>>
) : ViewModel
