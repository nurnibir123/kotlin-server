package org.example

data class HttpRequest(
    val method: String,
    val url: String,
    val version: String,
    val headers: Map<String, String>,
)
