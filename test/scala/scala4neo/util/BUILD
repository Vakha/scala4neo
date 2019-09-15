scala_library(
	sources=rglobs('*.scala'),
	dependencies=[
    	'3rdparty/jvm:scalatest',
    	'3rdparty/jvm:scalacheck',
        '3rdparty/jvm:scalaMock',
        'src/scala/scala4neo/util',
  	],
	excludes = [
		exclude('com.typesafe.scala-logging', 'scala-logging-api')
	]
)