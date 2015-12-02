import org.apache.commons.lang3.RandomStringUtils

class Reasoner {
  def ontology
  def ABoxen

  def Reasoner(ontology) {
    this.ontology = ontology
    this.ABoxen = [ ontology.ABox.clone() ]
  }

  def checkConsistency() {
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
        [ 'and', 'or', 'uq', 'eq' ].any { this."$it"(ABox) }
      }
    }
    println ''

    // ABoxens is 'complete' (contains no ABoxes with rules to apply), so now we will search for an 'open' (non-contradictory) ABox
    return ABoxen.any { ABox ->
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
      print '⊔'
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
      print '⊓'
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
  def eq(ABox) {
    def vRule = ABox.findAll { it.type == 'instance' && it.definition.type == 'operation' && it.definition.operation == '∃' }.find { eq ->
      !ABox.any { relation -> relation.type == 'relation' && relation.relation == eq.definition.relation && relation.left == eq.instance && ABox.find { it.type == 'instance' && it.definition == eq.definition.definition && it.instance == relation.right } }
    }

    if(vRule) {
      print '∃'
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
      ABox.find {
        def res = it.type == 'relation' && it.relation == uq.definition.relation && it.left == uq.instance && !ABox.any { ins -> ins.instance == it.right && ins.definition == uq.definition.definition }
        if(res) {
          relation = it
        }
        return res
      }
    }

    if(quantifier && relation) {
      print '∀'
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

  // Greater than equal to rule
  // Condition: A contains GTEnr.C(a) but there are no c1..cn with { r(a,c1), C(c1),..., r(a,cn), C(cn) } U { ci != cj | 1 <= i <= n, 1 <= j <= n, i != j }
  //  Or basically, there's a gte rule, but there aren't a bunch of relations and concepts expanded from them
  // Action: A' = A U { r(a, b1), C(b1) ... r(a, bn), C(bn) } U { bi != bj | 1 <= i <= n, 1 <= j <= n, i != j } where b1..bn are few individual names
  def gte(ABox) {
    def vRule = ABox.findAll { it.type == 'instance' && it.definition.type == 'operation' && it.definition.operation == '≥' }.find { gte ->
      def instances = [] 
      def relations = []

      // Find all the relevant relations
      ABox.each {
        if(it.type == 'relation' && it.left == gte.instance && !relations.contains(it.right)) {
          relations << it.right
        }
      }

      // Find all the concepts relevant to the relations
      ABox.each {
        if(it.type == 'instance' && relations.contains(it.instance) && !instances.contains(instance.instance) && instance.definition == gte.definition.definition) {
          instances << it.instance
        }
      }

      // We're pleased if we didn't find an instance for every relation, or if the number of instances and relations wasn't the same as the amount stipulated in gte
      return !(instances.size() == relations.size() && instances.size() == gte.definition.amount)
    }

    if(vRule) {
      def newABox = ABox.clone()

      // Add a new relation and concept for each amt in the gte rule
      (1..vRule.definition.amount).each {
        // Random instance name, from https://bowerstudios.com/node/1100
        def charset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()
        def newInstance = RandomStringUtils.random(5, charset.toCharArray())

        ABox << [
          'type': 'relation',
          'relation': vRule.definition.relation,
          'left': vRule.instance,
          'right': newInstance,
          'negate': false
        ] << [
          'type': 'instance',
          'definition': vRule.definition.definition,
          'instance': newInstance
          'negate': false
        ]
      }

      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << newABox
    }

    return vRule
  }

  // Less than equal to rule
  // Condition: A contains LTEnr.C(a), and there are b1,..,bn+1 with { r(a, b1), C(b1), ... r(a,bn+1), C(bn+1) } but NO { bi != bj | 1 <= i <= n, 1 <=j <= n, i != j}
  //   Or basically, there's an LTE rule, and a relation and concept for each amt in lte + 1. However, the instances of these b are not all distinct
  // Action: for all i != j with bi != bj NOT EXIST IN A, Ai,j = A[bi / bj] (rather, replace bi with bj)
  def lte(ABox) {
    def instances = [] 
    def relations = []

    def vRule = ABox.findAll { it.type == 'instance' && it.definition.type == 'operation' && it.definition.operation == '≤' }.find { lte ->
      instances = []
      relations = []

      // Find all the relevant relations
      ABox.each {
        if(it.type == 'relation' && it.left == gte.instance) {
          relations << it.right
        }
      }

      // Find all the concepts relevant to the relations
      ABox.each {
        if(it.type == 'instance' && relations.contains(it.instance) && instance.definition == gte.definition.definition) {
          instances << it.instance
        }
      }

      // We are happy if we found an instance for every relation, the number of these are n+1 and there is some duplicate in the instances
      return (instances.size() == relations.size()) && (instances.size() == lte.definition.amount + 1) && (instances.size() != instances.unique().size())
    }
  }
}
