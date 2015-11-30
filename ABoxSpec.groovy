class ABoxSpec extends DLSpec {
  def instance(Closure block, var) {
    def rule = [:]
    def dl = new DLSpec(isInstance: true)
    def code = block.rehydrate(dl, this, this)
    code.resolveStrategy = Closure.DELEGATE_ONLY
    code()

    rule['type'] = 'instance'
    rule['definition'] = dl.structure[0]
    rule['instance'] = var
    structure << rule
  }

  def relation(String relation, Object left, Object right) {
    def rule = [:]
    rule['type'] = 'relation'
    rule['relation'] = relation

    ['left':left, 'right':right].each { i, x ->
      if(x instanceof String) {
        rule[i] = [
          'type': 'literal',
          'value': x,
          'negate': negate
        ]
      } else if(x instanceof Closure) {
        def dl = new DLSpec(negate:negate)
        def code = x.rehydrate(dl, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        rule[i] = dl.structure[0]
      }
    }

    structure << rule
  }
}
