# galq

This is a relatively basic, yet complete ALCQ (Attributive Concept Language with Complements and Qualified cardinality restrictions) description logic reasoner, which supports verification of subsumption, consistency and satisfiability of ontologies with respect to given TBoxes and ABoxes by means of the Tableaux algorithm. It is not intended to be particularly quick, optimised, or bug-free, but as a relatively simple and working example of a description logic reasoner which can be used as an aid to understand the process by which these logical decision algorithms are achieved. It also implements a basic prefix notation domain specific language for the expression of description logic ontologies.

## Quickstart

Check out some of the examples:

```groovy examples/happysubsumption.groovy```

```groovy examples/sleepybiscuit.groovy```

```groovy examples/sadsubsumption.groovy```

```groovy examples/childconsistency.groovy```

Here is some example output:

![groovy examples/happysubsumption.groovy](http://i.imgur.com/1Q41Nuc.png "output")
