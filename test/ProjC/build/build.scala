import cbt._
import scala.collection.immutable.Seq
import java.io.File
// cbt:https://github.com/cvogt/cbt.git#65c917a36350b0273b2aa1bc5f3b238e7f657ab5
class Build(context: cbt.Context) extends BasicBuild(context){
  override def dependencies = super.dependencies ++
  Resolver( mavenCentral ).bind(
      ScalaDependency("com.typesafe.play", "play-json", "2.4.4"),
      MavenDependency("joda-time", "joda-time", "2.9.2"),
      // the below tests pom inheritance with dependencyManagement and variable substitution for pom properties
      MavenDependency("org.eclipse.jgit", "org.eclipse.jgit", "4.2.0.201601211800-r"),
      // the below tests pom inheritance with variable substitution for pom xml tag contents
      MavenDependency("com.spotify", "missinglink-core", "0.1.1"),
      // the below tests pom inheritance with variable substitution being parts of strings
      MavenDependency("cc.factorie","factorie_2.11","1.2")
  )
}
