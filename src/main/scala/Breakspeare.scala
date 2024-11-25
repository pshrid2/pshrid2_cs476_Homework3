import scala.collection.mutable

object Breakspeare {
  private val environment: mutable.Map[String, FuzzySet[Any]] = mutable.Map()
  type EnvironmentTable = mutable.Map[String, FuzzySet[Any]]

  // Scope management using a stack
  private var environmentStack: List[EnvironmentTable] = List(mutable.Map())

  // Scope functionality
  def enterScope(): Unit = {
    environmentStack = mutable.Map[String, FuzzySet[Any]]() :: environmentStack
    println(s"Entered scope. Stack size: ${environmentStack.size}")
  }

  def exitScope(): Unit = {
    environmentStack match {
      case head :: tail =>
        environmentStack = tail
        println(s"Exited scope. Stack size: ${environmentStack.size}")
      case Nil => throw new IllegalStateException("ScopeOutOfBounds")
    }
  }

  // Assign a fuzzy set to the current scope's environment
  def assign[T](name: String, fuzzySet: FuzzySet[T]): Unit = {
    environmentStack.head(name) = fuzzySet.asInstanceOf[FuzzySet[Any]]
    println(s"Assigned variable '$name' in current scope. Current scope: ${environmentStack.head}")
  }

  def createFuzzySet[T](membershipFunction: T => Double): FuzzySet[T] = {
    UserDefinedFuzzySet(membershipFunction)
  }

  def combine[T](setA: FuzzySet[T], operation: FuzzySetOperation, setB: Option[FuzzySet[T]], alpha: Option[Double] = None): FuzzySet[T] = {
    CombinedFuzzySet(setA, setB, operation, alpha)
  }


  // Real assign functionality
  case class Assign[T](name: String, fuzzySet: FuzzySet[T])

  def assignGate[T](assign_gate: Assign[T]): Unit = {
    this.assign(assign_gate.name, assign_gate.fuzzySet)
  }

  // Retrieve a fuzzy set from the current scope's environment
  def get(name: String): FuzzySet[Any] = {
    environmentStack.head.get(name) match {
      case Some(fuzzySet) => fuzzySet
      case None => throw new IllegalArgumentException(s"Variable '$name' not found in the current scope.")
    }
  }

  case class TestGate[T](fuzzySet: FuzzySet[T], value: T)
  def evaluate[T](testGate: TestGate[T]): Double = {
    testGate.fuzzySet.membership(testGate.value)
  }
}