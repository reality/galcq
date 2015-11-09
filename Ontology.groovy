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

  // Here we expand all of the concepts in the TBox and add them to the ABox
  def convertTBox() {
    TBox.each {
      ['left', 'right'].each { rule ->
        println 'expanding ' + rule
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
      def expander = TBox.find { gci -> // find a gci with a 0 which is == to our thing
        return rule == gci.left && gci != wgci
      }
      if(expander) {
        println 'matched ' + rule + ' with '  + expander
        rule = expander.right
        println rule
        return expand(rule, wgci)
      }

      return rule
    } else if(rule instanceof ArrayList) {
      rule.collect { expand(it, wgci) }
    } else {
      ['left', 'right'].collect { expand(rule[it], wgci) }
    }
  }
}

