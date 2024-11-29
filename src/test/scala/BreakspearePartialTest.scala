import org.scalatest.flatspec.AnyFlatSpec
import Breakspeare._

class BreakspearePartialTest extends AnyFlatSpec {

  "Partial evaluation" should "simplify expressions where possible" in {
    val expr = FuzzySetOp[Int](
      operation = FuzzySetOperation.Addition,
      lhs = Value(10),
      rhs = Some(Value(5))
    )

    val partiallyEvaluated = evaluate(expr, Map.empty)

    assert(partiallyEvaluated == Value(15))
  }

  "Nested expressions" should "simplify inner expressions where possible" in {
    val expr = FuzzySetOp[Int](
      operation = FuzzySetOperation.Multiplication,
      lhs = Value(2),
      rhs = Some(FuzzySetOp[Int](
        operation = FuzzySetOperation.Addition,
        lhs = Value(3),
        rhs = Some(Value(4))
      ))
    )

    val partiallyEvaluated = evaluate(expr, Map.empty)

    assert(partiallyEvaluated == Value(14))
  }

  "Resolvable variables" should "simplify expressions when possible" in {
    val expr = FuzzySetOp[Int](
      operation = FuzzySetOperation.Addition,
      lhs = Variable("x"),
      rhs = Some(Value(5))
    )

    val context = Map("x" -> Value(10))
    val partiallyEvaluated = evaluate(expr, context)

    assert(partiallyEvaluated == Value(15))
  }

  "IFTRUE partial evaluation" should "evaluate the thenBranch when the condition is true" in {
    val expr = IFTRUE[Int](
      condition = Value(true), // Condition evaluates to true
      thenBranch = FuzzySetOp(
        operation = FuzzySetOperation.Addition,
        lhs = Value(10),
        rhs = Some(Value(20))
      )
    )

    val partiallyEvaluated = evaluate(expr, Map.empty)

    assert(partiallyEvaluated == Value(30)) // Only the thenBranch is evaluated
  }

  "ELSERUN partial evaluation" should "evaluate the elseBranch when the condition is false" in {
    val expr = ELSERUN[Int](
      condition = Value(false), // Condition evaluates to false
      elseBranch = FuzzySetOp(
        operation = FuzzySetOperation.Multiplication,
        lhs = Value(5),
        rhs = Some(Value(4))
      )
    )

    val partiallyEvaluated = evaluate(expr, Map.empty)

    assert(partiallyEvaluated == Value(20)) // Only the elseBranch is evaluated
  }


}
