# galcq

This is a relatively complete and basic ALCQ (Attributive Concept Language with Complements and Qualified cardinality restrictions) description logic reasoner, which supports verification of subsumption, consistency and satisfiability of ontologies with respect to given TBoxes and ABoxes by means of the Tableaux algorithm, additionally providing a suitable model universe for the ontology (or a contradictory model in the case of failure). It is not intended to be particularly quick, optimised, or bug-free, but instead as a relatively simple and working example of a description logic reasoner which can be used as an aid to understand the process by which these logical decision algorithms are achieved. It also implements a prefix notation domain specific language for description logic, which allows the expression of ontologies without too much pain.

Here is some example output:

![groovy examples/happysubsumption.groovy](http://i.imgur.com/JbR0CcB.png "output")

## Quickstart

Check out some of the examples:

```groovy examples/happysubsumption.groovy```

```groovy examples/sleepybiscuit.groovy```

```groovy examples/sadsubsumption.groovy```

```groovy examples/childconsistency.groovy```

