# **Welcome to Breakspeare**
## To Be, To Not Be, To Maybe?
### By Pranav Shridhar

Breakspeare is an advanced domain-specific language (DSL) for designers of fuzzy logic systems. It allows users to 
create, manipulate, and evaluate fuzzy logic expressions and simulate fuzzy logic gates. With enhanced features 
like partial evaluation, conditional constructs, and object-oriented programming, Breakspeare provides a robust 
framework for building and optimizing complex fuzzy logic systems.

---

## **Key Features**
### 1. **Fuzzy Logic Constructs**
- **Fuzzy Sets**: Create fuzzy sets with custom membership functions that assign membership degrees to elements.
- **Fuzzy Operations**:
    - **Union, Intersection, and Complement**: Manipulate fuzzy sets with standard set-theoretic operations.
    - **Addition, Multiplication, and Difference**: Perform numerical operations on fuzzy sets.
    - **Alpha Cut**: Filter elements based on membership thresholds.

### 2. **Partial Evaluation**
- Breakspeare supports **partial evaluation**, allowing users to resolve expressions with known values while retaining unresolved variables for future evaluation.
- **Examples**:
  ```scala
  Multiply(Value(3), Multiply(Add(Value(5), Value(1)), Variable("x")))
  // Partially evaluates to:
  Multiply(Value(3), Multiply(Value(6), Variable("x")))
  ```

### 3. **Conditional Expressions**
- Implement **fuzzy conditional logic** with `IFTRUE` and `ELSERUN` constructs.
- **Examples**:
  ```scala
  val condition = MembershipCondition(setA, value = 0.7, threshold = 0.5)
  val expr = IFTRUE(condition, Value("High"), ELSERUN(condition, Value("Low")))
  ```

### 4. **Object-Oriented Features**
- **Classes**: Define reusable templates with variables, methods, and nested classes.
- **Inheritance**: Support for parent-child relationships.
- **Dynamic Dispatch**: Resolve methods dynamically at runtime.
- **Method Invocation**: Evaluate class methods with arguments and variables.

### 5. **Scope Management**
- Nested scopes ensure variable isolation and integrity during evaluation.

---

## **Usage Examples**

### **1. Creating and Evaluating Fuzzy Sets**
```scala
val setA = createFuzzySet((x: Double) => if (x > 0.5) 1.0 else 0.0)
val setB = createFuzzySet((x: Double) => if (x < 0.5) 1.0 else 0.0)

val unionSet = combine(setA, FuzzySetOperation.Union, Some(setB))
println(unionSet.membership(0.6)) // Output: 1.0
println(unionSet.membership(0.4)) // Output: 1.0
```

### **2. Partial Evaluation**
```scala
val expr = FuzzySetOp(FuzzySetOperation.Addition, Variable("x"), Some(Value(5)))
val context = Map("x" -> Value(10))

val result = evaluate(expr, context)
// Output: Value(15)
```

### **3. Using Conditionals**
```scala
val condition = MembershipCondition(setA, value = 0.7, threshold = 0.5)
val conditionalExpr = IFTRUE(condition, Value("High"), ELSERUN(condition, Value("Low")))

val result = evaluate(conditionalExpr, Map.empty)
// Output depends on the membership function of setA
```

### **4. Defining and Using Classes**
```scala
val myClass = Class[FuzzySet[Double]](
  name = "MyClass",
  parent = None,
  variables = List(ClassVar("threshold")),
  methods = List(
    Method(
      name = "isAboveThreshold",
      params = List(Parameter("x")),
      body = MembershipCondition(Variable("threshold"), Variable("x"), 0.8)
    )
  )
)

val instanceVars = Map(
  "threshold" -> createFuzzySet((x: Double) => if (x > 0.5) 1.0 else 0.0)
)

val args = Map("x" -> Value(0.7))
val result = invokeMethod("MyClass", instanceVars, "isAboveThreshold", args, Map("MyClass" -> myClass))
println(result) // Evaluates the method with the provided arguments
```

---

## **Testing**

Run the test suite with:
```bash
sbt test
```

### Test Coverage
- **Partial Evaluation**: Validates simplification of expressions.
- **Conditionals**: Ensures correct evaluation of `IFTRUE` and `ELSERUN`.
- **Fuzzy Set Operations**: Tests for union, intersection, complement, and more.
- **Class Functionality**: Checks variable/method resolution, inheritance, and method invocation.

---

## **Semantics of Partial Evaluation**
- **Evaluation Strategy**: Known values are computed, while unresolved variables remain as placeholders.
- **Example**:
  ```scala
  Add(Value(3), Multiply(Variable("x"), Value(5)))
  // Partial Evaluation with x=2:
  Add(Value(3), Value(10))
  ```

### Conditional Evaluation
- **IFTRUE**: Evaluates the `thenBranch` if the condition is true.
- **ELSERUN**: Evaluates the `elseBranch` if the condition is false.
- Both branches can undergo partial evaluation if unresolved variables are present.

