class Reasoner {
  def ontology
  def ABox

  def Reasoner(ontology) {
    this.ontology = ontology
    this.ABox = ontology.ABox
  }

  def checkConsistency() {
    def rulesToApply = true
    ontology.convertTBox() // Reduce everything to consistency problem

    while(rulesToApply) {
      println ABox
      rulesToApply = [ 'and' ].any { this."$it"() }
    }

    // now we will see
  }

  // AND rule:
  // Condition: A contains (C AND D)(a) but not both C(a) and D(a)
  // Action: A' = A UNION { C(a), D(a) }
  def and() {
    def vRule = ABox.findAll { it.definition.type == 'operation' && it.definition.operation == '⊓' }.find { instance ->
      def cA = ABox.find { it.definition == instance.definition.left }
      def cB = ABox.find { it.definition == instance.definition.right }

      return !(cA && cB)
    }

    if(vRule) {
      ABox << [
        'type': 'instance',
        'definition': vRule.definition.left,
        'instance': vRule.instance
      ]
      ABox << [
        'type': 'instance',
        'definition': vRule.definition.right,
        'instance': vRule.instance
      ]
      ABox = ABox - vRule
    }

    return vRule
  }
}

// [operation:⊓, left:[[operation:⊓, left:Sleepy, right:Squirrel]], right:Female]], instance:Woman],
