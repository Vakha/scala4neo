enumeratumVersion = '1.5.13'
circeVersion = '0.11.1'
sl4jVersion = '1.7.28'

jar_library(
    name='neo4jJavaDriver',
    jars=[
        jar(org='org.neo4j.driver', name='neo4j-java-driver', rev='1.7.5'),
    ]
)

jar_library(
    name='scalaJava8Compat',
    jars=[
        scala_jar(org='org.scala-lang.modules', name='scala-java8-compat', rev='0.9.0'),
    ]
)

jar_library(
    name='circeCore',
    jars=[
        scala_jar(org='io.circe', name='circe-core', rev=circeVersion),
    ]
)

jar_library(
    name='circeGeneric',
    jars=[
        scala_jar(org='io.circe', name='circe-generic', rev=circeVersion),
    ]
)

# TODO check if really used
jar_library(
    name='circeParser',
    jars=[
        scala_jar(org='io.circe', name='circe-parser', rev=circeVersion),
    ]
)

jar_library(
    name='typesafeConfig',
    jars=[
        jar(org='com.typesafe', name='config', rev='1.3.4'),
    ]
)

jar_library(
    name='sl4jApi',
    jars=[
        jar(org='org.slf4j', name='slf4j-api', rev=sl4jVersion),
    ]
)
jar_library(
    name='logback',
    jars=[
        jar(org='ch.qos.logback', name='logback-classic', rev='1.2.3'),
    ]
)

jar_library(
    name='enumeratum',
    jars=[
        scala_jar(org='com.beachape', name='enumeratum', rev=enumeratumVersion),
    ]
)

jar_library(
    name='enumeratumCirce',
    jars=[
        scala_jar(org='com.beachape', name='enumeratum-circe', rev=enumeratumVersion),
    ]
)

jar_library(
    name='scalatest',
    jars=[
        scala_jar(org='org.scalatest', name='scalatest', rev='3.0.8'),
    ]
)

jar_library(
    name='scalacheck',
    jars=[
        scala_jar(org='org.scalacheck', name='scalacheck', rev='1.13.4'),
    ]
)

jar_library(
    name='scalaMock',
    jars=[
        scala_jar(org='org.scalamock', name='scalamock-scalatest-support', rev='3.5.0'),
    ]
)

jar_library(
    name='monix',
    jars=[
        scala_jar(org='io.monix', name='monix', rev='3.0.0'),
    ]
)
