name := "ordenador-de-archivos"

version := "1.0"

scalaVersion := "2.12.1"

assemblyJarName := s"Ordenador de Archivos v${version.value}.jar"

libraryDependencies ++= Seq(
	"org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2",
	"com.typesafe" % "config" % "1.3.1",
	"org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
