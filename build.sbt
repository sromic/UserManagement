name := "UserManagment"

version := "1.0"

routesGenerator := InjectedRoutesGenerator

lazy val `usermanagment` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( cache , ws   , specs2 % Test,
  "com.typesafe.play" %% "play-slick" % "1.1.1",
  "com.h2database" % "h2" % "1.4.190",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  