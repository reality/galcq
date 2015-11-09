class TBoxSpec extends DLSpec {
  def gci(Object left, Object right) {
    def rule = [:]
    ['left':left, 'right':right].each { i, block ->
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
