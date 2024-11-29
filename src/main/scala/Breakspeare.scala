import scala.collection.mutable

object Breakspeare {
  // Generalized EnvironmentTable to support any type
  type EnvironmentTable[T] = mutable.Map[String, Expression[T]]

  // Scope management using a stack
  private val initialEnvironment: EnvironmentTable[Any] = mutable.Map[String, Expression[Any]]()
  private val environmentStack: List[EnvironmentTable[Any]] = List(initialEnvironment)

  def createFuzzySet[T](membershipFunction: T => Double): FuzzySet[T] = {
    UserDefinedFuzzySet(membershipFunction)
  }


  def initializeStack[T](): List[EnvironmentTable[T]] = List(mutable.Map[String, Expression[T]]())

  // Scope functionality
  def enterScope[T](stack: List[EnvironmentTable[T]]): List[EnvironmentTable[T]] =
    mutable.Map[String, Expression[T]]() :: stack

  def exitScope[T](stack: List[EnvironmentTable[T]]): List[EnvironmentTable[T]] =
    stack match {
      case _ :: tail => tail
      case Nil => throw new IllegalStateException("ScopeOutOfBounds")
    }

  // Assign an expression to the current scope's environment
  def assign[T](name: String, expression: Expression[T], stack: List[EnvironmentTable[T]]): List[EnvironmentTable[T]] = {
    val currentScope = stack.head
    currentScope(name) = expression
    stack
  }

  case class Assign[T](variable: String, expression: Expression[T])

  def assignGate[T](
                     assign_gate: Assign[T],
                     stack: List[EnvironmentTable[T]],
                     context: Map[String, Expression[T]] = Map.empty[String, Expression[T]] // Explicit type
                   ): List[EnvironmentTable[T]] = {
    // Partially evaluate the expression
    val evaluatedExpression = evaluate(assign_gate.expression, context)

    val currentScope = stack.head
    currentScope(assign_gate.variable) = evaluatedExpression

    stack
  }


  // Retrieve an expression from the current scope's environment
  def get[T](name: String, stack: List[EnvironmentTable[T]]): Expression[T] =
    stack.head.get(name) match {
      case Some(expression) => expression
      case None => throw new IllegalArgumentException(s"Variable '$name' not found in the current scope.")
    }

  case class IFTRUE[T](condition: Expression[Boolean], thenBranch: Expression[T]) extends Expression[T]
  case class ELSERUN[T](condition: Expression[Boolean], elseBranch: Expression[T]) extends Expression[T]


  // Evaluates both fully and partially
  def evaluate[T](expr: Expression[T], context: Map[String, Expression[T]]): Expression[T] = expr match {
    case Value(v) =>
      Value(v) // Already evaluated to a concrete value

    case Variable(name) =>
      context.getOrElse(name, throw new IllegalArgumentException(s"Variable $name not found in context"))

    case FuzzySetOp(operation, lhs, rhs, alpha) =>
      val left = evaluate(lhs, context)
      val right = rhs.map(evaluate(_, context))

      // Simplify only if both operands are fully evaluated
      (left, right) match {
        case (Value(lv), Some(Value(rv))) => combineGeneric(lv, operation, Some(rv), alpha)
        case (Value(lv), None)            => combineGeneric(lv, operation, None, alpha)
        case _                            => FuzzySetOp(operation, left, right, alpha) // Leave partially evaluated
      }

    case IFTRUE(condition, thenBranch) =>
      val conditionResult = evaluate(condition, context.asInstanceOf[Map[String, Expression[Boolean]]])
      conditionResult match {
        case Value(true)  => evaluate(thenBranch, context)
        case Value(false) => Value(null.asInstanceOf[T]) // No else branch; return default
        case _            => IFTRUE(conditionResult.asInstanceOf[Expression[Boolean]], thenBranch)
      }

    case ELSERUN(condition, elseBranch) =>
      val conditionResult = evaluate(condition, context.asInstanceOf[Map[String, Expression[Boolean]]])
      conditionResult match {
        case Value(false) => evaluate(elseBranch, context)
        case Value(true)  => Value(null.asInstanceOf[T]) // Else branch not run
        case _            => ELSERUN(conditionResult.asInstanceOf[Expression[Boolean]], elseBranch)
      }

    case MembershipCondition(fuzzySet, value, threshold) =>
      Value(fuzzySet.membership(value) > threshold)

    case _ =>
      throw new UnsupportedOperationException(s"Unsupported expression: $expr")
  }






  def combineGeneric[T](a: T, operation: FuzzySetOperation, b: Option[T], alpha: Option[Double]): Expression[T] = {
    (a, b, operation) match {
      case (a: Int, Some(b: Int), FuzzySetOperation.Addition) =>
        Value(a + b).asInstanceOf[Expression[T]] // Wrap result in Value
      case (a: Int, Some(b: Int), FuzzySetOperation.Multiplication) =>
        Value(a * b).asInstanceOf[Expression[T]] // Wrap result in Value
      case (a: Int, Some(b: Int), FuzzySetOperation.Difference) =>
        Value(a - b).asInstanceOf[Expression[T]] // Wrap result in Value
      case (a: FuzzySet[T], Some(b: FuzzySet[T]), _) =>
        Value(CombinedFuzzySet(a, Some(b), operation, alpha)).asInstanceOf[Expression[T]]
      case (a: FuzzySet[T], None, _) =>
        Value(CombinedFuzzySet(a, None, operation, alpha)).asInstanceOf[Expression[T]]
      case _ =>
        throw new UnsupportedOperationException(s"Combine not supported for types: $a, $b")
    }
  }

  // TestGate evaluation (specific for FuzzySet)
  case class TestGate[T](expression: Expression[T], value: T)

  def evaluateTestGate[T](testGate: TestGate[T]): T = {
    val evaluatedExpression = evaluate(testGate.expression, Map.empty)
    evaluatedExpression.asInstanceOf[Value[T]].v
  }

  // Method resolution and invocation for general expressions
  def resolveMethod[T](
                        className: String,
                        methodName: String,
                        classRegistry: Map[String, Class[T]]
                      ): Method[T] = {
    val (_, methods, _) = getClassWithInheritance(className, classRegistry)
    methods.find(_.name == methodName) match {
      case Some(method) => method
      case None => throw new IllegalArgumentException(s"Method $methodName not found in $className or its parents")
    }
  }

  def invokeMethod[T](
                       className: String,
                       instance: Map[String, Expression[T]],
                       methodName: String,
                       args: Map[String, Expression[T]],
                       classRegistry: Map[String, Class[T]]
                     ): Expression[T] = {
    // Resolve the method
    val method = resolveMethod(className, methodName, classRegistry)

    // Merge instance variables and arguments
    val methodContext = instance ++ args

    // Evaluate the method body
    evaluate(method.body, methodContext)
  }

  def getClassWithInheritance[T](
                                  className: String,
                                  classRegistry: Map[String, Class[T]]
                                ): (List[ClassVar[T]], List[Method[T]], List[Class[T]]) = {
    classRegistry.get(className) match {
      case Some(myClass) => resolveParent(myClass, classRegistry)
      case None => throw new IllegalArgumentException(s"Class $className not found")
    }
  }

  def resolveParent[T](
                        currentClass: Class[T],
                        classRegistry: Map[String, Class[T]]
                      ): (List[ClassVar[T]], List[Method[T]], List[Class[T]]) = {
    currentClass.parent match {
      case Some(parentName) =>
        classRegistry.get(parentName) match {
          case Some(parentClass) =>
            val (parentVars, parentMethods, parentNestedClasses) = resolveParent(parentClass, classRegistry)
            (
              parentVars ++ currentClass.variables,
              parentMethods ++ currentClass.methods,
              parentNestedClasses ++ currentClass.nestedClasses
            )
          case None => throw new IllegalArgumentException(s"Parent class $parentName not found")
        }
      case None =>
        (currentClass.variables, currentClass.methods, currentClass.nestedClasses)
    }
  }
}
