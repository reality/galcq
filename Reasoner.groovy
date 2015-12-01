import org.apache.commons.lang3.RandomStringUtils

class Reasoner {
  def ontology
  def ABoxen

  def Reasoner(ontology) {
    this.ontology = ontology
    this.ABoxen = [ ontology.ABox ]
  }

  def checkConsistency() {
  }

  // Non-empty TBox not allowed yet
  def checkSubsumption(emptyTBox) {
    println 'Testing subsumption for rules in following TBox: '
    ontology.printRules(ontology.TBox)
    println ''

    ontology.convertTBox() // Reduce everything to consistency problem

    println 'Reduced subsumption to satisfiability in the following ABox: '
    ontology.printRules(ontology.ABox)
    println ''

    println 'Testing satisfiability . . .'
    println ''

    def result = checkSatisfiability()

    println 'Satisfiable: ' + result
    println 'Subsumption: ' + !result
  }


  def checkSatisfiability() {
    def rulesToApply = true
    while(rulesToApply) {
      rulesToApply = ABoxen.find { ABox ->
        [ 'and', 'or', 'uq', 'eq' ].any { this."$it"(ABox) }
      }
    }

    // ABoxens is complete, so now we will search for an open ABox
    return ABoxen.any { ABox ->
      //println 'transformed abox'
      //ontology.printRules(ABox)
      ABox.any { rule ->
        if(rule.type != 'relation') {
          def nForm = rule.clone()
          nForm.definition.negate = !nForm.definition.negate
          ABox.findAll {
            return it.definition == nForm
          }.size() > 1 // to cover matching with self...
        }
      }
    }
  }

  // OR rule:
  // Condition: A contains (C OR D)(a) but neither C(a) or D(a)
  // Action: A' = A UNION { C(a) } and A'' = A UNION { D(a) }
  def or(ABox) {
    def vRule = ABox.findAll { it.type == 'instance' && it.definition.type == 'operation' && it.definition.operation == '⊔' }.find { instance ->
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

      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << firstNewABox << secondNewABox
    }

    return vRule
  }

  // AND rule:
  // Condition: A contains (C AND D)(a) but not both C(a) and D(a)
  // Action: A' = A UNION { C(a), D(a) }
  def and(ABox) {
    def vRule = ABox.findAll { it.type == 'instance' && it.definition.type == 'operation' && it.definition.operation == '⊓' }.find { instance ->
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

      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << newABox
    }

    return vRule
  }

  // TODO: Existential Quantifier rule:
  // Condition: A contains (EQr.C)(a) but no c for which { r(a,c), C(c) }
  // Action: A' = A UNION { r(a, b), C(b) } where b is a new individual name
  // TODO: Bad infinite loops

  def eq(ABox) {
    def vRule = ABox.findAll { it.type == 'instance' && it.definition.type == 'operation' && it.definition.operation == '∃' }.find { eq ->
      !ABox.any { relation -> relation.type == 'relation' && relation.relation == eq.definition.relation && relation.left == eq.instance && ABox.find { it.type == 'instance' && it.definition == eq.definition.definition && it.instance == relation.right } }
    }

    if(vRule) {
      // Random instance name, from https://bowerstudios.com/node/1100
      def charset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()
      def newInstance = RandomStringUtils.random(5, charset.toCharArray())

      def newABox = ABox.clone() << [
        'type': 'relation',
        'relation': vRule.definition.relation,
        'left': vRule.instance,
        'right': newInstance,
        'negate': false
      ] << [
        'type': 'instance',
        'definition': vRule.definition.definition, 
        'instance': newInstance,
        'negate': false
      ]

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
    def quantifier = ABox.findAll { it.type == 'instance' && it.definition.type == 'operation' && it.definition.operation == '∀' }.find { uq ->
    //println 'found ' + uq + ' with instance ' + uq.instance + ' and relation ' + uq.relation
      ABox.find {
      //println 'checking relation with left ' + it.left + ' and relation ' + it.relation
        def res = it.type == 'relation' && it.relation == uq.definition.relation && it.left == uq.instance && !ABox.any { ins -> ins.instance == it.right && ins.definition == uq.definition.definition }
        if(res) {
          relation = it
        }
        return res
      }
    }

    if(quantifier && relation) {
      /*println 'run uq on:'
      ontology.printRules([quantifier])
      println 'with abox'
      ontology.printRules(ABox)
      println ''*/

      def newABox = ABox.clone() << [
        'type': 'instance',
        'definition': quantifier.definition.definition,
        'instance': relation.right
      ]

      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << newABox
    }

    return quantifier && relation
  }
}
