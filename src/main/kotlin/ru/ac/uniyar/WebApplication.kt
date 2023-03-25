package ru.ac.uniyar

import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.lens.RequestContextLens
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Undertow
import org.http4k.server.asServer
import ru.ac.uniyar.config.AppConfig
import ru.ac.uniyar.config.databaseManagers.H2DatabaseManager
import ru.ac.uniyar.config.databaseManagers.connectToDatabase
import ru.ac.uniyar.config.databaseManagers.performMigrations
import ru.ac.uniyar.domain.operations.OperationHolder
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.domain.storage.User
import ru.ac.uniyar.web.filters.JwtTools
import ru.ac.uniyar.web.filters.authenticationFilter
import ru.ac.uniyar.web.filters.authorizationFilter
import ru.ac.uniyar.web.filters.errorFilter
import ru.ac.uniyar.web.handlers.categoryDelete
import ru.ac.uniyar.web.handlers.categoryForm
import ru.ac.uniyar.web.handlers.categoryList
import ru.ac.uniyar.web.handlers.editCategoryForm
import ru.ac.uniyar.web.handlers.editParentCategoryForm
import ru.ac.uniyar.web.handlers.editProductForm
import ru.ac.uniyar.web.handlers.logOutUser
import ru.ac.uniyar.web.handlers.orderProductsForm
import ru.ac.uniyar.web.handlers.parentCategoryDelete
import ru.ac.uniyar.web.handlers.parentCategoryForm
import ru.ac.uniyar.web.handlers.parentCategoryList
import ru.ac.uniyar.web.handlers.producerForm
import ru.ac.uniyar.web.handlers.producerList
import ru.ac.uniyar.web.handlers.productAddToCart
import ru.ac.uniyar.web.handlers.productDelete
import ru.ac.uniyar.web.handlers.productDetail
import ru.ac.uniyar.web.handlers.productForm
import ru.ac.uniyar.web.handlers.productList
import ru.ac.uniyar.web.handlers.shoppingCartList
import ru.ac.uniyar.web.handlers.signInForm
import ru.ac.uniyar.web.handlers.signUpForm
import ru.ac.uniyar.web.handlers.startPage
import ru.ac.uniyar.web.models.templates.ContextAwarePebbleTemplates
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

private fun app(
    view: ContextAwareViewRender,
    operationHolder: OperationHolder,
    salt: String,
    jwtTools: JwtTools,
    currentUserLens: RequestContextLens<User?>,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = routes(
    "/" bind startPage(
        view,
        operationHolder.listCategoryOperation,
    ),
    "/product" bind productList(
        view,
        operationHolder.listProductsOperation,
        operationHolder.productsOperation,
    ),
    "/product/new" bind productForm(
        view,
        operationHolder,
        permissionLens
    ),
    "/product/{number}" bind productDetail(
        view,
        operationHolder.productsOperation
    ),
    "/product/{number}/toCart/{countToCart}" bind productAddToCart(
        operationHolder.productsOperation,
        operationHolder.shoppingCartProductsOperation,
        currentUserLens,
        permissionLens
    ),
    "/product/{number}/delete" bind productDelete(
        operationHolder.productsOperation,
        permissionLens
    ),
    "product/{number}/edit" bind editProductForm(
        operationHolder.productsOperation,
        view,
        permissionLens
    ),
    "/parent_category" bind parentCategoryList(
        view,
        operationHolder.listParentCategoryOperation,
    ),
    "/parent_category/{parentCategoryName}/delete" bind parentCategoryDelete(
        operationHolder.parentCategoryOperation,
        permissionLens,
    ),
    "/parent_category/new" bind parentCategoryForm(
        view,
        operationHolder.parentCategoryOperation,
        permissionLens
    ),
    "/parent_category/{parentCategoryName}/edit" bind editParentCategoryForm(
        view,
        operationHolder.parentCategoryOperation,
        permissionLens
    ),
    "/category" bind categoryList(
        view,
        operationHolder.listCategoryOperation
    ),
    "/category/new" bind categoryForm(
        view,
        operationHolder.listParentCategoryOperation,
        operationHolder.categoryOperation,
        permissionLens
    ),
    "/category/{categoryName}/delete" bind categoryDelete(
        operationHolder.categoryOperation,
        permissionLens
    ),
    "/category/{categoryName}/edit" bind editCategoryForm(
        view,
        operationHolder.listParentCategoryOperation,
        operationHolder.categoryOperation,
        permissionLens
    ),
    "/shopping_cart" bind shoppingCartList(
        view,
        operationHolder.listCartOperation,
        operationHolder.shoppingCartProductsOperation,
        operationHolder.informationOperation,
        currentUserLens,
        permissionLens
    ),
    "/shopping_cart/order" bind orderProductsForm(
        view,
        operationHolder.shoppingCartProductsOperation,
        operationHolder.informationOperation,
        currentUserLens,
        permissionLens
    ),
    "/producer" bind producerList(
        view,
        operationHolder.listProducerOperation,
        permissionLens
    ),
    "/producer/new" bind producerForm(
        operationHolder.producerOperation,
        view,
        permissionLens
    ),
    "/signup" bind signUpForm(
        view,
        operationHolder.userOperation,
        salt,
    ),
    "/login" bind signInForm(
        view,
        operationHolder.userOperation,
        salt,
        jwtTools,
    ),
    "/logout" bind logOutUser(),
    static(ResourceLoader.Classpath("/ru/ac/uniyar/public/")),
)

fun main() {
    val appConfig = AppConfig.readConfiguration()
    val jwtTools = JwtTools(appConfig.salt, "ru.ac.uniyar.WebApplication")
    val h2databaseManager = H2DatabaseManager().initialize(appConfig.databaseConfig.port)
    performMigrations(appConfig.databaseConfig.jdbcConnection)
    val database = connectToDatabase(appConfig.databaseConfig.jdbcConnection)
    val operationHolder = OperationHolder(database)

    val contexts = RequestContexts()
    val currentUserLens: RequestContextLens<User?> = RequestContextKey.optional(contexts, "user")
    val permissionsLens: RequestContextLens<Permission> = RequestContextKey.required(contexts, "permission")

    val renderer = ContextAwarePebbleTemplates().hotReload("src/main/resources")
    val view = ContextAwareViewRender.withContentType(renderer, TEXT_HTML)
        .associateContextLens("currentUser", currentUserLens)
        .associateContextLens("permission", permissionsLens)

    val printingApp: HttpHandler = ServerFilters.InitialiseRequestContext(contexts)
        .then(errorFilter(view))
        .then(authenticationFilter(currentUserLens, operationHolder.userOperation, jwtTools))
        .then(authorizationFilter(currentUserLens, permissionsLens, operationHolder.permissionOperation))
        .then(app(view, operationHolder, appConfig.salt, jwtTools, currentUserLens, permissionsLens))
    val server = printingApp.asServer(Undertow(appConfig.webConfig.webPort)).start()

    println("Server started on http://localhost:" + server.port())
    println("Web-interface of database on http://${appConfig.databaseConfig.host}:${appConfig.databaseConfig.port}")
    println("Пароль для существующих пользователей: 1. Просто цифра 1.")

    readlnOrNull()
    h2databaseManager.stopServers()
}
