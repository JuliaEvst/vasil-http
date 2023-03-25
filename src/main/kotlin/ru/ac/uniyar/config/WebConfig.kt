package ru.ac.uniyar.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.int

data class WebConfig(
    val webPort: Int
) {
    companion object {
        val portLens = EnvironmentKey.int().required("web.port")

        fun createWebConfig(environment: Environment): WebConfig = WebConfig(environment.let(portLens))

        val webConfigDefaultEnv = Environment.defaults(
            portLens of 1515,
        )
    }
}
