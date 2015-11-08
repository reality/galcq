class Ontology {
  def ABox

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
    ABox = dl.structure
  }

  def checkConsistency() {
    return new Reasoner(this).checkConsistency()
  }
}
