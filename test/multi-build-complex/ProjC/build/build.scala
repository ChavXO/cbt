import cbt._
import java.net.URL
import java.io.File
import scala.collection.immutable.Seq

// cbt:https://github.com/mchav/cbt.git#b7e566a74afd3c9b262f57d6e5ca9a3ddd8bd81c

class Build( context: Context ) extends BasicBuild( context ){
  override def runClass = "CMain"
  override def dependencies = (
    super.dependencies // don't forget super.dependencies here
    ++
    Resolver( mavenCentral ).bind(
      MavenDependency( "com.lihaoyi", "ammonite-ops_2.11", "0.5.5" ), ScalaDependency( "org.scalafx", "scalafx", "8.0.60-R9" ) 
    )
  )
}
