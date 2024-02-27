package aniq.dev.apiusers.controller.response

class PagedResults<T>(
    val records: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Long
)