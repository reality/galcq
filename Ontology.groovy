import org.apache.commons.lang3.RandomStringUtils

class Ontology {
  def ABox
  def TBox
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
      ['left', 'right'].each { rule ->
        it[rule] = expand(it[rule], it)
      }

      // Negate the right, AND it with the left
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

  private expand(rule, wgci) {
    if(rule.type == 'literal') {
      def expander = TBox.find { gci -> // find a gci with a left which is == to our thing
        return rule == gci.left && gci != wgci
      }
      if(expander) {
        rule = expander.right
        return expand(rule, wgci)
      }
    } else {
      ['left', 'right'].each { rule[it] = expand(rule[it], wgci) }
    }
    return rule
  }

  // Convert to negation normal form recursively
  private negate(rule) {
    if(rule.type == 'literal') {
      rule.negate = !rule.negate
    } else {
      ['left', 'right'].each { negate(rule[it]) }
    }
  }
}
