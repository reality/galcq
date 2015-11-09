class Ontology {
  def ABox
  def TBox

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

      ABox << [
        'type': 'instance',
        'definition': it['right'],
        'instance': it['left']
      ]
    }
  }

  def checkConsistency() {
    return new Reasoner(this).checkConsistency()
  }

  def expand(rule, wgci) {
    if(rule instanceof String) {
      def expander = TBox.find { gci -> // find a gci with a left which is == to our thing
        return rule == gci.left && gci != wgci
      }
      if(expander) {
        rule = expander.right
        return expand(rule, wgci)
      }
    } else if(rule instanceof ArrayList) {
      rule.collect { expand(it, wgci) }
    } else {
      ['left', 'right'].each { rule[it] = expand(rule[it], wgci) }
    }
    return rule
  }
}
