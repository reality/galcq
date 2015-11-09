class TBoxSpec extends DLSpec {
  def gci(Object a, Object b) {
    def rule = [:]
    [a, b].eachWithIndex { block, i ->
      if(block instanceof String) {
        rule[i] = block
      } else if(block instanceof Closure) {
        def dl = new DLSpec(isInstance: true)
        def code = block.rehydrate(dl, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        rule[i] = dl.structure
      }
    }

    rule['type'] = 'gci'

    structure << rule
  }
}
