
// This should be the only thing the user should see
// "A fuzzy set A in a universe of discourse X is characterized by a membership function μ(x), where x∈X."
// So a FuzzySet need only to contain a membership function. No internal data structure necessary.
trait FuzzySet[T] {
  // Left as abstract. User should implement this for their use case.
  def membership(value: T): Double
}

// Set operations
enum FuzzySetOperation {
  case Union
  case Intersection
  case Complement
  case Difference
  case Addition
  case Multiplication
  case AlphaCut
}

// Ideally the user should never know this exists.
// If the user wants to make a FuzzySet, they have to define their own function. This is to generalise the use.
private case class UserDefinedFuzzySet[T](membershipFunction: T => Double) extends FuzzySet[T] {
  override def membership(value: T): Double = membershipFunction(value)
}

// User shouldn't see this either.
// This is the result of combining two sets under an operation. Logic is:
// - Combination of two fuzzy sets under an operator makes another set
// - That set has a membership function
// - This is that membership function
private case class CombinedFuzzySet[T](setA: FuzzySet[T], setB: Option[FuzzySet[T]], operation: FuzzySetOperation, alpha: Option[Double] = None) extends FuzzySet[T] {
  override def membership(value: T): Double = operation match {

    case FuzzySetOperation.Union => Math.max(setA.membership(value), setB.get.membership(value))
    case FuzzySetOperation.Intersection => Math.min(setA.membership(value), setB.get.membership(value))
    case FuzzySetOperation.Complement => 1.0 - setA.membership(value)
    case FuzzySetOperation.Difference => Math.max(0, setA.membership(value) - setB.get.membership(value))
    case FuzzySetOperation.Addition => Math.min(1.0, setA.membership(value) + setB.get.membership(value))
    case FuzzySetOperation.Multiplication => setA.membership(value) * setB.get.membership(value)
    case FuzzySetOperation.AlphaCut => if (setA.membership(value) >= alpha.get) 1.0 else 0.0
  }
}