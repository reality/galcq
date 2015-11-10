class Reasoner {
  def ontology
  def ABoxen

  def Reasoner(ontology) {
    this.ontology = ontology
    this.ABoxen = [ ontology.ABox ]
  }

  def checkConsistency() {
    def rulesToApply = true
    ontology.convertTBox() // Reduce everything to consistency problem

    while(rulesToApply) {
      rulesToApply = ABoxen.any { ABox ->
        [ 'and' ].any { this."$it"(ABox) }
      }
    }

    // ABox is complete, so now we will search for an open ABox
    println ABoxen
    println ABoxen.any { ABox ->
      !ABox.any { rule ->
        ABox.any {
          def nForm = rule.clone()
          nForm.negate = !nForm.negate
          return rule == nForm
        }
      }
    }
  }

  // AND rule:
  // Condition: A contains (C AND D)(a) but not both C(a) and D(a)
  // Action: A' = A UNION { C(a), D(a) }
  def and(ABox) {
    def vRule = ABox.findAll { it.definition.type == 'operation' && it.definition.operation == '⊓' }.find { instance ->
      def cA = ABox.find { it.definition == instance.definition.left }
      def cB = ABox.find { it.definition == instance.definition.right }

      return !(cA && cB)
    }

    if(vRule) {
      def newABox = ABox.clone() << [
        'type': 'instance',
        'definition': vRule.definition.left,
        'instance': vRule.instance
      ] << [
        'type': 'instance',
        'definition': vRule.definition.right,
        'instance': vRule.instance
      ]
      newABox.remove(newABox.indexOf(vRule))
      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << newABox
    }

    return vRule
  }
}

// [operation:⊓, left:[[operation:⊓, left:Sleepy, right:Squirrel]], right:Female]], instance:Woman],
