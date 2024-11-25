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
  def evaluate[T](testGate: TestGate[T]): Double = {
    testGate.fuzzySet.membership(testGate.value)
  }
}