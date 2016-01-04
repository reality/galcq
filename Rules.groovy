import org.apache.commons.lang3.RandomStringUtils

// These are the rules for the reasoner
class Rules {
  // OR rule:
  // Condition: A contains (C OR D)(a) but neither C(a) or D(a)
  // Action: A' = A UNION { C(a) } and A'' = A UNION { D(a) }
  static or(ABoxen, ABox) {
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
  static and(ABoxen, ABox) {
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
  static eq(ABoxen, ABox) {
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

  // ∀: Univeral Quantifier rule:
  // Condition: A contains (UQr.C)(a) and r(a, b) but not C(b)
  // Action: A' = A UNION {C(b)}
  static uq(ABoxen, ABox) {
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
  // Action: A' = A U { r(a, b1), C(b1) ... r(a, bn), C(bn) } U { bi != bj | 1 <= i <= n, 1 <= j <= n, i != j } where b1..bn are few individual names
  static gte(ABoxen, ABox) {
    def vRule = ABox.findAll { it.type == 'instance' && it.definition.type == 'operation' && it.definition.operation == '≥' }.find { gte ->
      def instances = [] 
      def relations = []
      def inequalities = []

      // Find all the relevant relations
      ABox.each {
        if(it.type == 'relation' && it.left == gte.instance && !relations.contains(it.right)) {
          relations << it.right
        }
      }

      // Find all the concepts relevant to the relations
      ABox.each {
        if(it.type == 'instance' && relations.contains(it.instance) && !instances.contains(it.instance) && it.definition == gte.definition.definition) {
          instances << it.instance
        }
      }

      // Find explicit inequalities
      ABox.each {
        if(it.type == 'distinction' && instances.contains(it.left) && instances.contains(it.right) && !inequalities.contains(it)) {
          inequalities << it
        }
      }
      
      def expectedDistinctions = 0
      (0..instances.size()-2).each { i ->
        (i+1..instances.size()-1).each { j ->
          expectedDistinctions++
        }
      }

      return !(instances.size() == relations.size() && instances.size() == gte.definition.amount && inequalities.size() == expectedDistinctions)
    }

    if(vRule) {
      print '≥'

      def newABox = ABox.clone()

      // Add a new relation and concept for each amt in the gte rule
      def newInstances = []
      (1..vRule.definition.amount).each {
        // Random instance name, from https://bowerstudios.com/node/1100
        def charset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()
        def newInstance = RandomStringUtils.random(5, charset.toCharArray())

        // Add the relation and the instance
        newABox << [
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

        newInstances << newInstance
      }

      // Add distinctions for generated items
      (0..newInstances.size()-2).each { i ->
        (i+1..newInstances.size()-1).each { j ->
          newABox << [
            'type': 'distinction',
            'left': newInstances[i],
            'right': newInstances[j],
            'negate': false
          ]
        }
      }

      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << newABox
    }

    return vRule
  }

  // Less than equal to rule
  // Condition: A contains LTEnr.C(a), and there are b1,..,bn+1 with { r(a, b1), C(b1), ... r(a,bn+1), C(bn+1) } but NO { bi != bj | 1 <= i <= n, 1 <=j <= n, i != j}
  // Action: for all i != j with bi != bj NOT EXIST IN A, Ai,j = A[bi / bj] (rather, replace bi with bj)
  static lte(ABoxen, ABox) {
    def instances
    def relations
    def inequalities
    def labels

    def vRule = ABox.findAll { it.type == 'instance' && it.definition.type == 'operation' && it.definition.operation == '≤' }.find { lte ->
      instances = 0
      relations = 0
      labels = []
      inequalities = []
      
      // Find all the relevant relations
      ABox.each {
        if(it.type == 'relation' && it.left.value == lte.instance && !labels.contains(it.right.value)) {
          labels << it.right.value
          relations++
        }
      }

      // Find all the concepts relevant to the relations
      ABox.each {
        if(it.type == 'instance' && labels.contains(it.instance) && it.definition == lte.definition.definition) {
          instances++
        }
      }

       // Find explicit inequalities
      ABox.each {
        if(it.type == 'distinction' && labels.contains(it.left) && labels.contains(it.right) && !inequalities.contains(it)) {
          inequalities << it
        }
      }
      
      def expectedDistinctions = 0
      (0..relations-2).each { i ->
        (i+1..relations-1).each { j ->
          expectedDistinctions++
        }
      }

      // We are happy if we found an instance for every relation, the number of these are n+1 and there is some duplicate in the instances
      // If the relation is top, we don't have to have 
      return (instances == relations || lte.definition.definition.type == 'literal' && lte.definition.definition.value == '⊤') &&
        (relations == lte.definition.amount + 1) && (inequalities.size() < expectedDistinctions)
    }

    if(vRule) {
      print '≤'

      def newABox = ABox.clone() 
      (0..relations-2).each { i ->
        (i+1..relations-1).each { j ->
          // if there is no inequality for this pair of instances
          if(!inequalities.find { ((it.left == labels[i] && it.right == labels[j]) || (it.left == labels[j] && it.right == labels[i])) }) {
            // we will simply update the instance of j to = the instance of i
            newABox.each { rule ->
              if(rule.instance && rule.instance == labels[j]) {
                rule.instance = labels[i]
              } else if(rule.type == 'relation' && rule.right.value == labels[j]) {
                rule.right.value = labels[i]
              }
            }
          }
        }
      }

      ABoxen.remove(ABoxen.indexOf(ABox))
      ABoxen << newABox
    }
  }
}
