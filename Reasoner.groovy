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

    println 'abox rules'
    ontology.printRules(ontology.ABox)
    println ''

    while(rulesToApply) {
      rulesToApply = ABoxen.any { ABox ->
        [ 'and', 'or' ].any { this."$it"(ABox) }
      }
    }

    // ABox is complete, so now we will search for an open ABox
    println ABoxen.any { ABox ->
      println 'transformed abox'
      ontology.printRules(ABox)
      println 'consistent: ' + !ABox.any { rule ->
        def nForm = rule.clone()
        nForm.definition.negate = !nForm.definition.negate
        ABox.findAll {
          return it == nForm
        }.size() > 1 // to cover matching with self...
      }
    }
  }

  // OR rule:
  // Condition: A contains (C OR D)(a) but neither C(a) or D(a)
  // Action: A' = A UNION { C(a) } and A'' = A UNION { D(a) }
  def or(ABox) {
    def vRule = ABox.findAll { it.definition.type == 'operation' && it.definition.operation == '⊔' }.find { instance ->
      def cA = ABox.find { it.definition == instance.definition.left && it.instance == instance.instance }
      def cB = ABox.find { it.definition == instance.definition.right && it.instance == instance.instance }

      return !(cA || cB)
    }

    if(vRule) {
      def firstNewABox = ABox.clone() << [
        'type': 'instance',
        'definition': vRule.definition.left,
        'instance': vRule.instance
      ]
      def secondNewABox = ABox.clone() << [
        'type': 'instance',
        'definition': vRule.definition.right,
        'instance': vRule.instance
      ]

      firstNewABox.remove(firstNewABox.indexOf(vRule))
      secondNewABox.remove(firstNewABox.indexOf(vRule))

      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << firstNewABox << secondNewABox
    }

    return vRule
  }

  // AND rule:
  // Condition: A contains (C AND D)(a) but not both C(a) and D(a)
  // Action: A' = A UNION { C(a), D(a) }
  def and(ABox) {
    def vRule = ABox.findAll { it.definition.type == 'operation' && it.definition.operation == '⊓' }.find { instance ->
      def cA = ABox.find { it.definition == instance.definition.left && it.instance == instance.instance }
      def cB = ABox.find { it.definition == instance.definition.right && it.instance == instance.instance }

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

  // TODO: Existential Quantifier rule:
  // Condition: A contains (EQr.C)(a) but no c for which { r(a,c), C(c) }
  // Action: A' = A UNION { r(a, b), C(b) } where b is a new individual name
  def eq(ABox) {
    def vRule = ABox.findAll { it.definition.type == 'operation' && it.definition.operation == '∃' }.find { eq ->
      return !ABox.find { relation -> relation.type == 'relation' && relation.relation == eq.relation && relation.left == eq.instance && ABox.find { it.type == 'instance' && it.definition == eq.left && it.instance == relation.right } }
    }

    if(vRule) {
      // create new indidivual name
      def newABox = ABox.clone() << [
        'type': 'relation',
        'left': vRule.instance,
        'right': newInstance
      ] << [
        'type': 'instance',
        'definition': vRule.left, 
        'instance': newInstance
      ]
      newABox.remove(newABox.indexOf(vRule))
      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << newABox
    }

    return vRule
  }

  // TODO: Univeral Quantifier rule:
  // Condition: A contains (UQr.C)(a) and r(a, b) but not C(b)
  // Action: A' = A UNION {C(b)}
  def uq(ABox) {
    def relation 
    def quantifier = ABox.findAll { it.definition.type == 'operation' && it.definition.operation == '∀' }.find { uq ->
      ABox.find {
        def res = it.type == 'relation' && it.relation == uq.relation && it.left == uq.instance && !ABox.find { ins -> ins.instance != it.right && ins.definition == uq.definition.right }
        if(res) {
          relation = it
        }
        return res
      }
    }

    if(quantifier && relation) {
      def newABox = ABox.clone() << [
        'type': 'instance',
        'definition': quantifier.definition.right,
        'instance': relation.right
      ]
      newABox.remove(newABox.indexOf(vRule))
      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << newABox
    }

    return vRule
  }
}
