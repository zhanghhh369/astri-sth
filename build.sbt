name := """astri-haihua"""
organization := "astri.org"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.11"


libraryDependencies += guice

libraryDependencies += javaWs

libraryDependencies += "net.sf.json-lib" % "json-lib" % "2.4" classifier "jdk15"

libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.3"

libraryDependencies += "com.alibaba" % "fastjson" % "1.2.47"

libraryDependencies += "org.elasticsearch.client" % "elasticsearch-rest-client" % "7.6.2"

// Testing libraries for dealing with CompletionStage...
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies ++= Seq(
  javaJpa,
  javaJdbc,
  "org.hibernate" % "hibernate-entitymanager" % "5.3.7.Final",
  "org.hibernate" % "hibernate-jpamodelgen" % "5.3.7.Final" % "provided",
  // "org.mariadb.jdbc" % "mariadb-java-client" % "2.3.0"
  //"mysql" % "mysql-connector-java" % "5.1.47"
  "org.postgresql" % "postgresql" % "42.2.5"
)
// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

// Kafka libraries
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.1.0"
libraryDependencies += "org.springframework.kafka" % "spring-kafka" % "2.2.2.RELEASE"

// Apache POI
libraryDependencies += "org.apache.poi" % "poi" % "4.1.0"
libraryDependencies += "org.apache.poi" % "poi-ooxml" % "4.1.0"
libraryDependencies += "commons-codec" % "commons-codec" % "1.12"
libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.3"
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1"

// JSch
libraryDependencies += "com.jcraft" % "jsch" % "0.1.55"

// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile, compile in Test)

PlayKeys.externalizeResources := false

// generate the JPA metaModel
val generated_src = "target/generated_src"

val generated_src_file = new java.io.File(generated_src)

val newfile = generated_src_file.mkdirs()

//cleanKeepFiles += generated_src_file

javacOptions ++= Seq("-s", generated_src)

javaOptions in Test += "-Dconfig.file=conf/test.conf"

PlayKeys.externalizeResources := false