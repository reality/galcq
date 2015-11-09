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
      it = it.collect { rule ->
        return expand(rule)
      }

      ABox << [
        'type': 'instance',
        'definition': it[0],
        'instance': it[1]
      ]
    }
  }

  def checkConsistency() {
    return new Reasoner(this).checkConsistency()
  }

  // Apparently it's naughty to put this in there?
  def expand(rule) {
    [0, 1].collect { 
      if(rule[it] instanceof String) {
        def expander = TBox.find { gci -> // find a gci with a 0 which is == to our thing
          return rule[it] == gci[0]
        }
        if(expander) {
          rule[it] = expander[1]
          return expand(rule[it])
        }
        return rule[it]
      }
      return expand(rule[it])
    }
  }
}

