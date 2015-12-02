class Reasoner {
  def ontology
  def ABoxen

  def Reasoner(ontology) {
    this.ontology = ontology
    this.ABoxen = [ ontology.ABox.clone() ]
  }

  // Is the ABox A non-contradictory?
  // A is consistent with respect to T iff it has a model which is also a model of T
  def checkConsistency() {
    println 'Testing consistency of ontology:'

    println '- TBox -'
    ontology.printRules(ontology.TBox)

    println '- ABox -'
    ontology.printRules(ontology.ABox)

    println ''
    println 'Reducing to satisfiability'
    println ''
    ontology.expandABox()

    def result = checkSatisfiability()

    println 'Consistency: ' + result
    println 'With the following model: '
  }

  // Non-empty TBox not allowed yet
  def checkSubsumption(emptyTBox) {
    println 'Testing subsumption for rules in following TBox: '
    ontology.printRules(ontology.TBox)
    println ''

    ontology.convertTBox() // Reduce everything in the TBox into the ABox, allowing us to test subsumption via satisfiability

    println 'Reduced subsumption to satisfiability in the following ABox: '
    ontology.printRules(ontology.ABox)
    println ''

    def result = checkSatisfiability()

    println ''
    println 'Subsumption: ' + !result
  }

  // Test satisfiability by applying all the rules we can, then checking if we have an ABox with contradictions.
  // Note that generating rules, EQ and GTE are applied with lowest priority.
  def checkSatisfiability() {
    print 'Testing satisfiability . . . '

    def rulesToApply = true
    while(rulesToApply) {
      rulesToApply = ABoxen.find { ABox ->
        [ 'and', 'or', 'uq', 'lte', 'eq', 'gte' ].any { Rules."$it"(ABoxen, ABox) }
      }
    }
    println ''

    // ABoxens is 'complete' (contains no ABoxes with rules to apply), so now we will search for an 'open' (non-contradictory) ABox
    return ABoxen.any { ABox ->
      ABox.any { rule ->
        def nForm = rule.clone()
        if(rule.definition) {
          nForm.definition.negate = !nForm.definition.negate
        } else {
          nForm.negate = !nForm.negate
        }

        !ABox.any {
          return it == nForm
        }
      }
    }
  }
}
