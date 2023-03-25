package ru.ac.uniyar.web.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.domain.storage.Information
import ru.ac.uniyar.domain.storage.ShoppingCart

data class ShoppingCartListVM(
    val shoppingCartProducts: List<ShoppingCart>,
    val path: String,
    val page: Int,
    val lastPage: Int,
    val infoList: List<Information>,
    val userAddress: String,
) : ViewModel
