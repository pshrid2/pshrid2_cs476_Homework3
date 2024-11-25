import org.scalatest.flatspec.AnyFlatSpec

import Breakspeare._

class BreakspeareTest extends AnyFlatSpec {

  "createFuzzySet" should "work correctly with Double" in {
    val fuzzySet: FuzzySet[Double] = createFuzzySet[Double]((x: Double) => if (x > 0.5) 1.0 else 0.0)
    assert(fuzzySet.membership(0.6) == 1.0)
    assert(fuzzySet.membership(0.4) == 0.0)
  }

  "assignGate" should "store and retrieve a FuzzySet in the environment" in {
    enterScope()
    val fuzzySet: FuzzySet[Double] = createFuzzySet[Double]((x: Double) => if (x > 25) 1.0 else 0.0)
    assignGate(Assign("temperature", fuzzySet))

    val retrievedSet = get("temperature").asInstanceOf[FuzzySet[Double]]
    assert(retrievedSet.membership(30.0) == 1.0)
    assert(retrievedSet.membership(20.0) == 0.0)
    exitScope()
  }

  "combine" should "perform Union operation correctly with Double" in {
    val setA: FuzzySet[Double] = createFuzzySet[Double]((x: Double) => if (x > 0.5) 1.0 else 0.0)
    val setB: FuzzySet[Double] = createFuzzySet[Double]((x: Double) => if (x < 0.5) 1.0 else 0.0)

    val unionSet: FuzzySet[Double] = combine[Double](setA, FuzzySetOperation.Union, Some(setB))
    assert(unionSet.membership(0.6) == 1.0)
    assert(unionSet.membership(0.4) == 1.0)
  }

  "TestGate" should "evaluate membership correctly" in {
    val fuzzySet: FuzzySet[Double] = createFuzzySet[Double]((x: Double) => if (x > 25) 1.0 else 0.0)
    val testGate: TestGate[Double] = TestGate(fuzzySet, 30.0)
    val result: Double = evaluate(testGate)
    assert(result == 1.0)
  }

  "Nested scope" should "isolate variables correctly" in {
    // Enter the outer scope
    enterScope()
    val outerFuzzySet = createFuzzySet((x: Double) => if (x > 25) 1.0 else 0.0)
    assignGate(Assign("temperature", outerFuzzySet))

    // Enter the inner scope
    enterScope()
    val innerFuzzySet = createFuzzySet((x: Double) => if (x < 10) 1.0 else 0.0)
    assignGate(Assign("temperature", innerFuzzySet))

    // Retrieve the fuzzy set in the inner scope
    val retrievedInnerSet = get("temperature").asInstanceOf[FuzzySet[Double]]
    println(s"Inner scope 'temperature' membership for 5.0: ${retrievedInnerSet.membership(5.0)}")
    assert(retrievedInnerSet.membership(5.0) == 1.0)  // Should be inner scope set
    assert(retrievedInnerSet.membership(30.0) == 0.0)

    // Exit the inner scope
    exitScope()

    // Retrieve the fuzzy set in the outer scope
    val retrievedOuterSet = get("temperature").asInstanceOf[FuzzySet[Double]]
    println(s"Outer scope 'temperature' membership for 30.0: ${retrievedOuterSet.membership(30.0)}")
    assert(retrievedOuterSet.membership(30.0) == 1.0) // Should be outer scope set
    assert(retrievedOuterSet.membership(20.0) == 0.0)

    // Exit the outer scope
    exitScope()
  }

  "De Morgan's laws" should "hold for fuzzy sets" in {
    // Create two fuzzy sets
    val setA: FuzzySet[Double] = createFuzzySet[Double]((x: Double) => if (x > 0.5) 1.0 else 0.0)
    val setB: FuzzySet[Double] = createFuzzySet[Double]((x: Double) => if (x < 0.5) 1.0 else 0.0)

    // Calculate the union of setA and setB
    val unionAB = combine[Double](setA, FuzzySetOperation.Union, Some(setB))
    // Calculate the complement of the union
    val complementUnionAB = combine[Double](unionAB, FuzzySetOperation.Complement, None)

    // Calculate the complement of setA and setB
    val complementA = combine[Double](setA, FuzzySetOperation.Complement, None)
    val complementB = combine[Double](setB, FuzzySetOperation.Complement, None)
    val intersectionComplementAB = combine[Double](complementA, FuzzySetOperation.Intersection, Some(complementB))

    for (x <- Seq(0.3, 0.5, 0.7)) {
      assert(complementUnionAB.membership(x) == intersectionComplementAB.membership(x))
    }

    // Calculate the intersection of setA and setB
    val intersectionAB = combine[Double](setA, FuzzySetOperation.Intersection, Some(setB))
    val complementIntersectionAB = combine[Double](intersectionAB, FuzzySetOperation.Complement, None)
    val unionComplementAB = combine[Double](complementA, FuzzySetOperation.Union, Some(complementB))

    for (x <- Seq(0.3, 0.5, 0.7)) {
      assert(complementIntersectionAB.membership(x) == unionComplementAB.membership(x))
    }
  }
}