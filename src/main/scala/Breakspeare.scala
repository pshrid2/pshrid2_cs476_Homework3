import scala.collection.mutable

object Breakspeare {
  private val environment: mutable.Map[String, FuzzySet[Any]] = mutable.Map()
  type EnvironmentTable = mutable.Map[String, FuzzySet[Any]]

  // Scope management using a stack
  private val initialEnvironment: EnvironmentTable = mutable.Map()
  private val environmentStack: List[EnvironmentTable] = List(initialEnvironment)


  def initializeStack(): List[EnvironmentTable] = List(mutable.Map[String, FuzzySet[Any]]())


  // Scope functionality
  def enterScope(stack: List[EnvironmentTable]): List[EnvironmentTable] = {
    mutable.Map[String, FuzzySet[Any]]() :: stack
  }

  def exitScope(stack: List[EnvironmentTable]): List[EnvironmentTable] = {
    stack match {
      case _ :: tail => tail
      case Nil => throw new IllegalStateException("ScopeOutOfBounds")
    }
  }

  // Assign a fuzzy set to the current scope's environment
  def assign[T](name: String, fuzzySet: FuzzySet[T], stack: List[EnvironmentTable]): List[EnvironmentTable] = {
    val currentScope = stack.head
    currentScope(name) = fuzzySet.asInstanceOf[FuzzySet[Any]]
    stack
  }

  def createFuzzySet[T](membershipFunction: T => Double): FuzzySet[T] = {
    UserDefinedFuzzySet(membershipFunction)
  }

  def combine[T](setA: FuzzySet[T], operation: FuzzySetOperation, setB: Option[FuzzySet[T]], alpha: Option[Double] = None): FuzzySet[T] = {
    CombinedFuzzySet(setA, setB, operation, alpha)
  }


  // Real assign functionality
  case class Assign[T](name: String, fuzzySet: FuzzySet[T])

  def assignGate[T](assign_gate: Assign[T], stack: List[EnvironmentTable]): List[EnvironmentTable] = {
    assign(assign_gate.name, assign_gate.fuzzySet, stack)
  }

  // Retrieve a fuzzy set from the current scope's environment
  def get(name: String, stack: List[EnvironmentTable]): FuzzySet[Any] = {
    stack.head.get(name) match {
      case Some(fuzzySet) => fuzzySet
      case None => throw new IllegalArgumentException(s"Variable '$name' not found in the current scope.")
    }
  }

  case class TestGate[T](fuzzySet: FuzzySet[T], value: T)
  def evaluate[T](expr: Any, context: Map[String, FuzzySet[T]] = Map.empty): Any = expr match {
    // Handle TestGate exactly as before
    case testGate: TestGate[T] =>
      testGate.fuzzySet.membership(testGate.value)

    // Handle Expression-based evaluation
    case expression: Expression[FuzzySet[T]] => expression match {
      case Value(v) => v // Return constant FuzzySet
      case Variable(name) =>
        // Resolve variable from the context
        context.getOrElse(name, throw new IllegalArgumentException(s"Variable $name not found"))
      case FuzzySetOp(operation, lhs, rhs, alpha) =>
        // Evaluate left-hand and right-hand sides
        val leftSet = evaluate(lhs, context).asInstanceOf[FuzzySet[T]]
        val rightSet = rhs.map(evaluate(_, context).asInstanceOf[FuzzySet[T]])
        // Delegate to FuzzySet.combine
        combine(leftSet, operation, rightSet, alpha)
    }

    // If expr is neither TestGate nor Expression
    case _ =>
      throw new IllegalArgumentException("Unsupported input for evaluate")
  }

  def evaluate[T](testGate: TestGate[T]): Double = {
    testGate.fuzzySet.membership(testGate.value)
  }

}