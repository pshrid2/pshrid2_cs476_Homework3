# Welcome to Breakspeare
## To be, to not be, to maybe?
### By Pranav Shridhar

Breakspeare is a domain-specific language (DSL) designed to facilitate the 
creation, manipulation, and evaluation of fuzzy sets within the context of fuzzy 
logic. It allows users to work with sets that have varying degrees of membership 
rather than binary inclusion. With the addition of classes, methods, and inheritance, 
Breakspeare now supports object-oriented programming constructs tailored for fuzzy logic applications.

---

### **Key Features**
1. **Fuzzy Set Creation:** Define fuzzy sets using flexible membership functions that
determine how much an element belongs to a set. These sets are not limited to a single data type, 
allowing for membership functions defined over any generic type. Member functions are abstract when created on their own.


2. **Scope Management:** Supports nested scoping, enabling the creation and manipulation of fuzzy sets within distinct 
contexts. This feature ensures that variables in different scopes do not interfere with each other, maintaining data integrity across operations.

3. **Fuzzy Set Operations:** Breakspeare includes an extensive set of operations to manipulate fuzzy sets:
   - Union: Combines two fuzzy sets, reflecting the maximum membership degree for each element.
   - Intersection: Computes the minimum membership degree for elements across two sets.
   - Complement: Calculates the negation of a fuzzy set, reversing the membership values.
   - Addition, Multiplication, and Difference: Provides numerical manipulations of fuzzy sets for more advanced operations.
   - Alpha Cut: Produces a crisp set that contains elements whose membership values exceed a specified threshold.

4. **Object-Oriented Features:**
   - **Classes**:
      - Define named scopes with variables, methods, and nested classes.
      - Supports variables with flexible membership functions and methods operating on fuzzy sets.
   - **Inheritance**:
      - Classes can inherit methods and variables from parent classes.
   - **Dynamic Dispatch**:
      - Methods are resolved dynamically, supporting polymorphism.
   - **Method Invocation**:
      - Methods can include operations on fuzzy sets, returning results based on the class context.

5. **Abstraction:** There are two kinds of FuzzySets, but thankfully you won't have to worry about any of them. 
You need only to interact with the FuzzySet type.

---

### **How to Start Breakspeare**
#### Prerequisites:
- **Scala**: Version 2.13.x or later.
- **SBT (Simple Build Tool)**: Version 1.5.x or later.
- **JDK (Java Development Kit)**: Version 8 or later.

#### Steps:
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-folder>
   ```

2. Build and test the project:
   ```bash
   sbt clean compile test
   ```

3. Run the main application:
   ```bash
   sbt run
   ```

4. Modify `main.scala` to add your custom logic for fuzzy sets, classes, and methods:
   ```scala
   object Main extends App {
     val setA = Breakspeare.createFuzzySet((x: Double) => if (x > 0.5) 1.0 else 0.0)
     val setB = Breakspeare.createFuzzySet((x: Double) => if (x < 0.5) 1.0 else 0.0)

     val unionSet = Breakspeare.combine(setA, FuzzySetOperation.Union, Some(setB))

     println(s"Membership of 0.6 in Union Set: ${unionSet.membership(0.6)}")
     println(s"Membership of 0.4 in Union Set: ${unionSet.membership(0.4)}")
   }
   ```

---

### **How to Create and Evaluate Expressions in Breakspeare**

#### **1. Creating Fuzzy Sets**
Fuzzy sets in Breakspeare are defined using custom membership functions. These functions determine the degree of membership for any given element in the set.

**Example:**
```scala
val setA = createFuzzySet((x: Double) => if (x > 0.5) 1.0 else 0.0)
val setB = createFuzzySet((x: Double) => if (x < 0.5) 1.0 else 0.0)
```

#### **2. Combining Fuzzy Sets Using Operations**
```scala
val unionSet = combine(setA, FuzzySetOperation.Union, Some(setB))
val complementSet = combine(setA, FuzzySetOperation.Complement, None)
```

#### **3. Assigning Fuzzy Sets to Variables**
```scala
enterScope()
assignGate(Assign("temperature", setA))
```

#### **4. Retrieving and Evaluating Fuzzy Sets**
```scala
val retrievedSet = get("temperature").asInstanceOf[FuzzySet[Double]]
val membershipValue = retrievedSet.membership(0.6)
```

---

### **Using Object-Oriented Features**

#### **1. Defining a Class**
```scala
val simpleClass = Class[FuzzySet[Double]](
  name = "SimpleClass",
  parent = None,
  variables = List(ClassVar("threshold")),
  methods = List(
    Method(
      name = "greaterThanThreshold",
      params = List(Parameter("x")),
      body = FuzzySetOp(
        operation = FuzzySetOperation.AlphaCut,
        lhs = Variable("x"),
        alpha = Some(0.5)
      )
    )
  ),
  nestedClasses = List.empty[Class[FuzzySet[Double]]]
)
```

#### **2. Inheritance**
```scala
val parentClass = Class[FuzzySet[Double]](
  name = "ParentClass",
  parent = None,
  variables = List(ClassVar("parentThreshold")),
  methods = List(
    Method(
      name = "checkThreshold",
      params = List(),
      body = Variable("parentThreshold")
    )
  )
)

val childClass = Class[FuzzySet[Double]](
  name = "ChildClass",
  parent = Some("ParentClass"),
  variables = List(),
  methods = List(
    Method(
      name = "overrideMethod",
      params = List(),
      body = FuzzySetOp(
        operation = FuzzySetOperation.Complement,
        lhs = Variable("parentThreshold")
      )
    )
  )
)
```

#### **3. Method Invocation**
```scala
val instanceVars = Map(
  "threshold" -> UserDefinedFuzzySet((x: Double) => if (x > 0.5) 1.0 else 0.0)
)

val args = Map(
  "x" -> UserDefinedFuzzySet((x: Double) => if (x > 0.7) 1.0 else 0.0)
)

val result = Breakspeare.invokeMethod(
  className = "SimpleClass",
  instance = instanceVars,
  methodName = "greaterThanThreshold",
  args = args,
  classRegistry = Map("SimpleClass" -> simpleClass)
)

assert(result.membership(0.8) == 1.0) // Above threshold
assert(result.membership(0.4) == 0.0) // Below threshold
```
