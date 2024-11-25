case class Class[T] (
                      name: String,
                      parent: Option[String],
                      variables: List[ClassVar[T]],
                      methods: List[Method[T]]
                      )


case class ClassVar[T](name: String)

case class Method[T](
                      name: String,
                      params: List[Parameter[T]],
                      body: Expression[T]
                    )

case class Parameter[T](name: String)

sealed trait Expression[T]
case class Value[T](v: T) extends Expression[T]
case class Variable[T](name: String) extends Expression[T]

case class FuzzySetOp[T](
                          operation: FuzzySetOperation,       // Operation type (from FuzzySet.scala)
                          lhs: Expression[T],                // Left-hand side operand
                          rhs: Option[Expression[T]] = None, // Optional right-hand side operand (for binary ops)
                          alpha: Option[Double] = None       // Optional threshold for AlphaCut
                        ) extends Expression[T]