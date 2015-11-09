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
    def vRule = ontology.ABox.findAll { return it.operation == '⊓' }.find { instance ->
      def cA = ontology.ABox.find { rule ->
         
      }
      def cB

      return (cA && cB) == false
    }

    if(vRule) {
      
    }

    return vRule
  }
}
