class Reasoner {
  def ontology

  def Reasoner(ontology) {
    this.ontology = ontology
  }

  def checkConsistency() {
    
  }

  // AND rule:
  // Condition: A contains (C AND D)(a) but not both C(a) and D(a)
  // Action: A' = A UNION { C(a), D(a) }
  def and() {
    def vRule = ontology.ABox.findAll { it.definition.operation == '⊓' }.find { instance ->
      def cA = ontology.ABox.find { it.definition == instance.definition.left }
      def cB = ontology.ABox.find { it.definition == instance.definition.right }

      return !(cA && cB)
    }

    if(vRule) {
      
    }

    return vRule
  }
}

// [operation:⊓, left:[[operation:⊓, left:Sleepy, right:Squirrel]], right:Female]], instance:Woman],
