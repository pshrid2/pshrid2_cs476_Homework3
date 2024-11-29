import org.scalatest.flatspec.AnyFlatSpec
import Breakspeare._

class BreakspeareClassTest extends AnyFlatSpec {

  "Class creation" should "support variables and methods" in {
    // Create a simple class with variables and methods
    val classVars = List(ClassVar[FuzzySet[Int]]("x"), ClassVar[FuzzySet[Int]]("y"))
    val methodBody: Expression[FuzzySet[Int]] = Value(createFuzzySet((x: Int) => if (x > 10) 1.0 else 0.0))
    val methods: List[Method[FuzzySet[Int]]] = List(Method("fuzzyMethod", List(Parameter[FuzzySet[Int]]("input")), methodBody))
    val nestedClasses: List[Class[FuzzySet[Int]]] = List.empty // Explicitly define an empty list for nested classes
    val myClass = Class[FuzzySet[Int]]("MyClass", None, classVars, methods, nestedClasses)

    assert(myClass.name == "MyClass")
    assert(myClass.variables.map(_.name) == List("x", "y"))
    assert(myClass.methods.map(_.name) == List("fuzzyMethod"))
  }

  "Class inheritance" should "support parent-child relationships and variable/method resolution" in {
    // Define parent class with variables and methods
    val parentVars = List(ClassVar[FuzzySet[Int]]("parentVar"))
    val parentMethodBody: Expression[FuzzySet[Int]] = Value(createFuzzySet((x: Int) => if (x > 20) 1.0 else 0.0))
    val parentMethods: List[Method[FuzzySet[Int]]] = List(Method("parentMethod", List(Parameter[FuzzySet[Int]]("input")), parentMethodBody))
    val parentClass = Class[FuzzySet[Int]]("ParentClass", None, parentVars, parentMethods, List.empty[Class[FuzzySet[Int]]]) // Explicitly specify empty list of nested classes

    // Define child class that extends parent class
    val childVars = List(ClassVar[FuzzySet[Int]]("childVar"))
    val childMethodBody: Expression[FuzzySet[Int]] = Value(createFuzzySet((x: Int) => if (x < 5) 1.0 else 0.0))
    val childMethods: List[Method[FuzzySet[Int]]] = List(Method("childMethod", List(Parameter[FuzzySet[Int]]("input")), childMethodBody))
    val childClass = Class[FuzzySet[Int]]("ChildClass", Some("ParentClass"), childVars, childMethods, List.empty[Class[FuzzySet[Int]]]) // Explicitly specify empty list of nested classes

    // Assertions for the child class
    assert(childClass.name == "ChildClass")
    assert(childClass.parent.contains("ParentClass"))
    assert(childClass.variables.map(_.name) == List("childVar"))
    assert(childClass.methods.map(_.name) == List("childMethod"))
  }

  "Class with nested classes" should "support composition" in {
    val nestedVars = List(ClassVar[FuzzySet[Int]]("nestedVar"))
    val nestedMethods: List[Method[FuzzySet[Int]]] = List(Method("nestedMethod", List(Parameter[FuzzySet[Int]]("input")), Value(createFuzzySet((x: Int) => if (x == 42) 1.0 else 0.0))))
    val nestedClass = Class[FuzzySet[Int]]("NestedClass", None, nestedVars, nestedMethods, List.empty)

    val parentVars = List(ClassVar[FuzzySet[Int]]("parentVar"))
    val parentMethods: List[Method[FuzzySet[Int]]] = List(Method("parentMethod", List(Parameter[FuzzySet[Int]]("input")), Value(createFuzzySet((x: Int) => if (x > 20) 1.0 else 0.0))))
    val parentClass = Class[FuzzySet[Int]]("ParentClassWithNested", None, parentVars, parentMethods, List(nestedClass))

    assert(parentClass.nestedClasses.map(_.name) == List("NestedClass"))
    assert(parentClass.nestedClasses.head.variables.map(_.name) == List("nestedVar"))
    assert(parentClass.nestedClasses.head.methods.map(_.name) == List("nestedMethod"))
  }

  "assignGate and get" should "store and retrieve expressions in the environment" in {
    val stack = initializeStack[Int]()
    val expr: Expression[Int] = Value(42)
    val updatedStack = assignGate(Assign("testVar", expr), stack)

    val retrieved = get("testVar", updatedStack)

    assert(retrieved == Value(42))
  }

}
