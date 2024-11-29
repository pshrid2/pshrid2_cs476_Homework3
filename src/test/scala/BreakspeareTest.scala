import org.scalatest.flatspec.AnyFlatSpec
import Breakspeare._

class BreakspeareTest extends AnyFlatSpec {

  "assignGate and get" should "store and retrieve expressions in the environment" in {
    var stack = initializeStack[Int]()
    stack = enterScope(stack)
    val expr = Value(42)


    stack = assignGate(Assign("myVar", expr), stack)
    val retrievedExpr = get("myVar", stack).asInstanceOf[Value[Int]]
    assert(retrievedExpr.v == 42)
    stack = exitScope(stack)
  }

  "Scopes" should "isolate variables correctly" in {
    var stack = initializeStack[Int]()
    stack = enterScope(stack)
    stack = assignGate(Assign("x", Value(10)), stack)

    stack = enterScope(stack)

    stack = assignGate(Assign("x", Value(20)), stack)

    // Retrieve the variable in the inner scope
    val innerX = get("x", stack).asInstanceOf[Value[Int]]
    assert(innerX.v == 20)
    stack = exitScope(stack)
    val outerX = get("x", stack).asInstanceOf[Value[Int]]
    assert(outerX.v == 10)
  }

  "Arithmetic" should "evaluate addition correctly" in {
    val context: Map[String, Expression[Int]] = Map("x" -> Value(10), "y" -> Value(5))
    val expr: Expression[Int] = FuzzySetOp(FuzzySetOperation.Addition, Variable("x"), Some(Variable("y")))

    val result = evaluate[Int](expr, context).asInstanceOf[Value[Int]]
    assert(result.v == 15)
  }

  "Arithmetic2" should "evaluate multiplication correctly" in {
    val context: Map[String, Expression[Int]] = Map("a" -> Value(4), "b" -> Value(3))
    val expr: Expression[Int] = FuzzySetOp(FuzzySetOperation.Multiplication, Variable("a"), Some(Variable("b")))

    val result = evaluate(expr, context).asInstanceOf[Value[Int]]
    assert(result.v == 12)
  }

  "Variables" should "throw an error when a variable is not found in the context" in {
    val context: Map[String, Expression[Int]] = Map("a" -> Value(4))
    val expr: Expression[Int] = Variable("missing")

    intercept[IllegalArgumentException] {
      evaluate(expr, context)
    }
  }

  "De Morgan's Laws" should "hold for complements of unions and intersections" in {
    // Create two fuzzy sets
    val setA: FuzzySet[Int] = createFuzzySet((x: Int) => if (x > 5) 1.0 else 0.0)
    val setB: FuzzySet[Int] = createFuzzySet((x: Int) => if (x < 10) 1.0 else 0.0)

    // Define context with FuzzySet expressions
    val context: Map[String, Expression[FuzzySet[Int]]] = Map(
      "setA" -> Value(setA),
      "setB" -> Value(setB)
    )

    // Expressions for De Morgan's laws
    val unionExpr: Expression[FuzzySet[Int]] =
      FuzzySetOp(FuzzySetOperation.Union, Variable("setA"), Some(Variable("setB")))
    val complementUnionExpr: Expression[FuzzySet[Int]] =
      FuzzySetOp(FuzzySetOperation.Complement, unionExpr, None)

    val complementAExpr: Expression[FuzzySet[Int]] =
      FuzzySetOp(FuzzySetOperation.Complement, Variable("setA"), None)
    val complementBExpr: Expression[FuzzySet[Int]] =
      FuzzySetOp(FuzzySetOperation.Complement, Variable("setB"), None)
    val intersectionComplementExpr: Expression[FuzzySet[Int]] =
      FuzzySetOp(FuzzySetOperation.Intersection, complementAExpr, Some(complementBExpr))

    // Evaluate both expressions
    val complementUnionResult = evaluate(complementUnionExpr, context).asInstanceOf[Value[FuzzySet[Int]]].v
    val intersectionComplementResult = evaluate(intersectionComplementExpr, context).asInstanceOf[Value[FuzzySet[Int]]].v

    // Assert the membership values for specific points
    Seq(4, 7, 11).foreach { x =>
      assert(complementUnionResult.membership(x) == intersectionComplementResult.membership(x))
    }
  }



}
