package scala4neo.query.pagination

case class PageRequest(
  offset: Int,
  size: Int,
  sort: Seq[SortOrder] = Seq(PageRequest.DefaultSortById)
) {
  require(offset >= 0, "Offset value should be greater or equal 0")

  def setSortOrder(sortOrders: Seq[SortOrder]): PageRequest =
    this.copy(sort = sortOrders)

  def querySize: Int = if (size == Int.MaxValue) size else size + 1

}

object PageRequest {

  val DefaultSortById: SortOrder = 'id.asc
  val DefaultSize: Int = 20

  val ALL = PageRequest(
    offset = 0,
    size = Int.MaxValue,
    sort = Seq.empty
  )

  val DEFAULT = PageRequest(
    offset = 0,
    size = DefaultSize
  )
}