# scala4neo
Advanced scala wrapper for Neo4j java driver

Based on idea of usage powerfull Cypher query language without building DSL for it. 
Instead plain queries are wrapped in simple case class that carries return type information as well.
Queries could be execute using Database (by read and write methods) or usign GenericRepository that will
help you to decode your objects if implicit codecs are provided. 

## Query
```scala
case class PersonByIds(ids: Seq[UUID], 
                       page: PageRequest) extends CypherNQuery.Aux[Person] {
  override val query = s"MATCH (n: Person) WHERE n.id IN {personIds} RETURN n"
  override val pageRequest = Some(page)
  override val paramMap: Map[String, Any] = Map(
    "personIds" -> ids.map(_.toString).asJava
  )
}
```

## Codec
```scala
implicit val personValueCodec: ValueCodec[Person] =
  new NodeCodec[Person] {
    override def convert(record: Value): Person =
      Person(
        id = UUID.fromString(record.get("id").asString),
        name = record.get("name").asString
      )
  }
```

## Usage
```scala
val config = ConfigFactory.load()
val db: Database = new NeoDatabase(config)
val genericRepo = new GenericRepository(db)

val pageRequest = PageRequest(offset = 0, size = 5, sort = Seq("n.name".asc))
val query = PersonByIds(ids, pageRequest)

val res: Task[Seq[Person]] = genericRepo.findAllBy(query)
```
