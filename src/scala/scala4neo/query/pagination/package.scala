package scala4neo.query

package object pagination {

  /**
    * For example sort = 'id.desc
    */
  implicit class RichSortOrderSymbol(field: Symbol) {
    def asc: SortOrder = SortOrder(field.name, Direction.ASC)
    def desc: SortOrder = SortOrder(field.name, Direction.DESC)
  }

  implicit class RichSortOrderString(field: String) {
    def asc: SortOrder = SortOrder(field, Direction.ASC)
    def desc: SortOrder = SortOrder(field, Direction.DESC)
  }

}
