import org.scalatest.flatspec.AnyFlatSpec

import Breakspeare._


class BreakspeareClassTest extends AnyFlatSpec {

  "Class Definitions" should "define variables and methods correctly" in {
    val simpleClass = Class[FuzzySet[Double]](
      name = "SimpleClass",
      parent = None,
      variables = List(ClassVar("threshold")),
      methods = List(
        Method(
          name = "getThreshold",
          params = List(),
          body = Variable("threshold")
        )
      ),
      nestedClasses = List.empty[Class[FuzzySet[Double]]] // Ensure type consistency
    )

    assert(simpleClass.name == "SimpleClass")
    assert(simpleClass.variables.map(_.name) == List("threshold"))
    assert(simpleClass.methods.head.name == "getThreshold")
  }

  "Method Invocation" should "invoke methods and return the correct result" in {
    val simpleClass = Class[FuzzySet[Double]](
      name = "SimpleClass",
      parent = None,
      variables = List(ClassVar("threshold")),
      methods = List(
        Method(
          name = "greaterThanThreshold",
          params = List(Parameter("x")),
          body = FuzzySetOp(
            operation = FuzzySetOperation.AlphaCut,
            lhs = Variable("x"),
            alpha = Some(0.5)
          )
        )
      ),
      nestedClasses = List.empty[Class[FuzzySet[Double]]] // Explicit type for empty list
    )

    val classRegistry = Map("SimpleClass" -> simpleClass)

    // Wrap the functions into FuzzySet
    val instanceVars = Map(
      "threshold" -> UserDefinedFuzzySet((x: Double) => if (x > 0.5) 1.0 else 0.0)
    )

    val args = Map(
      "x" -> UserDefinedFuzzySet((x: Double) => if (x > 0.7) 1.0 else 0.0)
    )

    val result = Breakspeare.invokeMethod(
      className = "SimpleClass",
      instance = instanceVars,
      methodName = "greaterThanThreshold",
      args = args,
      classRegistry = classRegistry
    )

    assert(result.membership(0.8) == 1.0) // Above threshold
    assert(result.membership(0.4) == 0.0) // Below threshold
  }


  "Single Variable Access" should "retrieve and evaluate a variable correctly" in {
    val simpleClass = Class[FuzzySet[Double]](
      name = "SimpleClass",
      parent = None,
      variables = List(ClassVar("threshold")),
      methods = List(
        Method(
          name = "getThreshold",
          params = List(),
          body = Variable("threshold")
        )
      ),
      nestedClasses = List.empty[Class[FuzzySet[Double]]]
    )

    val classRegistry = Map("SimpleClass" -> simpleClass)
    val instanceVars = Map(
      "threshold" -> UserDefinedFuzzySet((x: Double) => if (x > 0.5) 1.0 else 0.0)
    )

    val result = Breakspeare.invokeMethod(
      className = "SimpleClass",
      instance = instanceVars,
      methodName = "getThreshold",
      args = Map.empty,
      classRegistry = classRegistry
    )

    assert(result.membership(0.6) == 1.0)
    assert(result.membership(0.4) == 0.0)
  }

  "Simple Inheritance" should "use a parent class's method correctly" in {
    val parentClass = Class[FuzzySet[Double]](
      name = "ParentClass",
      parent = None,
      variables = List(ClassVar("parentThreshold")),
      methods = List(
        Method(
          name = "checkParentThreshold",
          params = List(),
          body = Variable("parentThreshold")
        )
      ),
      nestedClasses = List.empty[Class[FuzzySet[Double]]]
    )

    val childClass = Class[FuzzySet[Double]](
      name = "ChildClass",
      parent = Some("ParentClass"),
      variables = List(),
      methods = List(),
      nestedClasses = List.empty[Class[FuzzySet[Double]]]
    )

    val classRegistry = Map(
      "ParentClass" -> parentClass,
      "ChildClass" -> childClass
    )

    val instanceVars = Map(
      "parentThreshold" -> UserDefinedFuzzySet((x: Double) => if (x > 0.3) 1.0 else 0.0)
    )

    val result = Breakspeare.invokeMethod(
      className = "ChildClass",
      instance = instanceVars,
      methodName = "checkParentThreshold",
      args = Map.empty,
      classRegistry = classRegistry
    )

    assert(result.membership(0.4) == 1.0)
    assert(result.membership(0.2) == 0.0)
  }


  "Fuzzy Set Combination" should "combine two fuzzy sets correctly using Union" in {
    val combineClass = Class[FuzzySet[Double]](
      name = "CombineClass",
      parent = None,
      variables = List(),
      methods = List(
        Method(
          name = "combineSets",
          params = List(Parameter("setA"), Parameter("setB")),
          body = FuzzySetOp(
            operation = FuzzySetOperation.Union,
            lhs = Variable("setA"),
            rhs = Some(Variable("setB"))
          )
        )
      ),
      nestedClasses = List.empty[Class[FuzzySet[Double]]]
    )

    val classRegistry = Map("CombineClass" -> combineClass)

    val instanceVars = Map.empty[String, FuzzySet[Double]]
    val args = Map(
      "setA" -> UserDefinedFuzzySet((x: Double) => if (x > 0.5) 1.0 else 0.0),
      "setB" -> UserDefinedFuzzySet((x: Double) => if (x < 0.3) 1.0 else 0.0)
    )

    val result = Breakspeare.invokeMethod(
      className = "CombineClass",
      instance = instanceVars,
      methodName = "combineSets",
      args = args,
      classRegistry = classRegistry
    )

    assert(result.membership(0.6) == 1.0) // From setA
    assert(result.membership(0.2) == 1.0) // From setB
    assert(result.membership(0.4) == 0.0) // Neither setA nor setB
  }


}
