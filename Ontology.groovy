import org.apache.commons.lang3.RandomStringUtils

class Ontology {
  def ABox = []
  def TBox = []
  def rGen = new Random()

  def setABox(@DelegatesTo(DLSpec) Closure cl) {
    def dl = new ABoxSpec()
    def code = cl.rehydrate(dl, this, this)
    code.resolveStrategy = Closure.DELEGATE_ONLY
    code()
    ABox = dl.structure
  }

  def setTBox(@DelegatesTo(DLSpec) Closure cl) {
    def dl = new TBoxSpec()
    def code = cl.rehydrate(dl, this, this)
    code.resolveStrategy = Closure.DELEGATE_ONLY
    code()
    TBox = dl.structure
  }

  // Here we use concept expansion on the generalised concept inclusions in the TBox, 
  //   converting them to fully expanded ABox axioms
  def convertTBox() {
    TBox.each {
      // Reduce subsumption to satisfiability via concept expansion
      ['left', 'right'].each { rule ->
        it[rule] = expand(it[rule], it)
      }

      // Reduce satisfiability to consistency
      //   Negate the right, AND it with the left
      negate(it['right'])
      def newDefinition = [
        'type': 'operation',
        'operation': '⊓',
        'left': it['left'],
        'right': it['right']
      ]

      // Random instance name, from https://bowerstudios.com/node/1100
      def charset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()
      def instance = RandomStringUtils.random(5, charset.toCharArray())

      ABox << [
        'type': 'instance',
        'definition': newDefinition,
        'instance': instance
      ]
    }
  }

  def checkConsistency() {
    return new Reasoner(this).checkConsistency()
  }

  def checkSubsumption(emptyTBox) {
    return new Reasoner(this).checkSubsumption(emptyTBox)
  }

  private expand(rule, wgci) {
    if(rule.type == 'literal') {
      def expander = TBox.find { gci -> // find a gci with a left which is == to our thing
        return rule == gci.left && gci != wgci && gci.type != 'literal'
      }
      if(expander) {
        rule = expander.right.clone()
        return expand(rule, wgci)
      }
    } else if(rule.type == 'operation' && (rule.operation == '∃' || rule.operation == '∀' )) {
      rule.definition = expand(rule.definition, wgci)
    } else {
      ['left', 'right'].each { rule[it] = expand(rule[it], wgci).clone() }
    }
    return rule
  }

  // Convert to negation normal form recursively
  private negate(rule) {
    if(rule.type == 'literal') {
      rule.negate = !rule.negate
    } else if(rule.type == 'operation' && (rule.operation == '∃' || rule.operation == '∀' )) {
      negate(rule.definition)
    } else {
      ['left', 'right'].each { negate(rule[it]) }
    }
  }

  static printRules(rules) {
    rules.each { rule ->
      if(rule.type == 'instance') {
        printRule(rule.definition)
        printRule(rule.instance)
      } else if(rule.type == 'gci') {
        printRule(rule.left)
        print ' ⊑ '
        printRule(rule.right)
      } else if(rule.type == 'distinction') {
        printRule(rule.left)
        print ' ≠ '
        printRule(rule.right)
      } else {
        printRule(rule)
      }
      println ''
    }
  }

  private static printRule(rule) {
    if(rule instanceof String) {
      print "($rule)"
    } else if(rule.type == 'literal') {
      if(rule.negate) {
        print '¬'
      }
      print rule.value
    } else if(rule.type == 'operation' && (rule.operation == '∃' || rule.operation == '∀' )) {
      print "$rule.operation$rule.relation"+"."
      printRule(rule.definition)
    } else if(rule.type == 'operation' && (rule.operation == '≤' || rule.operation == '≥' )) {
      print "$rule.operation$rule.amount$rule.relation"+"."
      printRule(rule.definition)
    } else if(rule.type == 'relation') {
      printRule(rule.relation)	
      print "($rule.left, $rule.right)"
	} else {
      print '('
      printRule(rule.left)
      print " $rule.operation "
      printRule(rule.right)
      print ')'
    }
  }
}
