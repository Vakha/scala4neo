package scala4neo.query.pagination

case class Page[+T](
  offset: Int,
  dataSeq: Seq[T],
  hasNext: Boolean
) {

  def hasPrevious: Boolean = offset != 0

  def startIndex: Option[Int] = dataSeq.headOption.map(_ => offset)

  def endIndex: Option[Int] = dataSeq.headOption.map(_ => offset + dataSeq.size - 1)

  def map[T1](f: T => T1): Page[T1] = copy(
    dataSeq = dataSeq.map(f)
  )

}

object Page {

  def apply[T](dataSeq: Seq[T], pageRequest: PageRequest): Page[T] = {
    if (dataSeq.size - 1 > pageRequest.size) {
      throw new IllegalStateException(
        s"Invalid length of dataSeq: ${dataSeq.size}. " +
          s"It must be less or equals to request size value: ${pageRequest.size} + 1"
      )
    }
    val hasNext = dataSeq.size - 1 == pageRequest.size
    val resultSeq = if (hasNext) dataSeq.dropRight(1) else dataSeq
    Page(pageRequest.offset, resultSeq, hasNext)
  }

  def empty[T]: Page[T] = {
    Page(0, Seq.empty, hasNext = false)
  }

  def empty[T](pageRequest: PageRequest): Page[T] = Page(
    dataSeq = Seq.empty[T],
    pageRequest
  )
}