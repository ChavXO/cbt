package cbt
import cbt.eval.Eval
trait DynamicOverrides extends BaseBuild{
  private val twitterEval = cached("eval"){
    new Eval{
      override lazy val impliedClassPath: List[String] = context.parentBuild.get.classpath.strings.toList//new ScalaCompilerDependency( context.cbtHasChanged, context.paths.mavenCache, scalaVersion ).classpath.strings.toList
      override def classLoader = DynamicOverrides.this.getClass.getClassLoader
    }
  }

  protected [cbt] def overrides: String = ""

  // TODO: add support for Build inner classes
  def newBuild[T <: DynamicOverrides:scala.reflect.ClassTag]: T = newBuild[T](context)("")
  def newBuild[T <: DynamicOverrides:scala.reflect.ClassTag](body: String): T = newBuild[T](context)(body)
  def newBuild[T <: DynamicOverrides:scala.reflect.ClassTag](context: Context)(body: String): T = {
    val mixinClass = scala.reflect.classTag[T].runtimeClass
    assert(mixinClass.getTypeParameters.size == 0)
    val mixin = if(
      mixinClass == classOf[Nothing]
      || mixinClass.getSimpleName == "Build"
    ) "" else " with "+mixinClass.getName
    import scala.collection.JavaConverters._
    val parent = Option(
      if(this.getClass.getName.startsWith("Evaluator__"))
        this.getClass.getName.dropWhile(_ != '$').drop(1).stripSuffix("$1")
      else
        this.getClass.getName
    ).getOrElse(
      throw new Exception( "You cannot have more than one newBuild call on the Stack right now." )
    )
    val overrides = "" // currently disables, but can be used to force overrides everywhere
    if(mixin == "" && overrides == "" && body == ""){
      // TODO: is it possible for the contructor to have the wrong signature and
      // thereby produce a pretty hard to understand error message here?
      this.getClass
        .getConstructor(classOf[Context])
        .newInstance(context)
        .asInstanceOf[T]
    } else {
      val baseName = "DynamicBuild" + System.currentTimeMillis
      val overrideName = baseName+"Overrides"
      val (finalName, code) = if(overrides == ""){
        (
          baseName,
          s"""
          class $baseName(context: _root_.cbt.Context)
            extends $parent(context)$mixin{
              $body
          }
          """
        )
    } else {
        (
          overrideName,
          s"""
          class $baseName(context: _root_.cbt.Context)
            extends $parent(context)$mixin{
              $body
          }
          class $overrideName(context: _root_.cbt.Context)
            extends $baseName(context){
              $overrides
          }
          """
        )
      }
      logger.dynamic("Dynamically generated code:\n" ++ code)
      twitterEval.compile(code)
      val createBuild = twitterEval.apply[Context => T](s"new $finalName(_: _root_.cbt.Context)",false)
      createBuild( context ).asInstanceOf[T]
    }
  }
}
