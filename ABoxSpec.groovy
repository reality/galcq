class ABoxSpec extends DLSpec {
  def instance(definition, var) {
    def rule = [:]
    rule['type'] = 'instance'
    rule['instance'] = var
    rule['negate'] = negate

    if(definition instanceof String) {
      rule['definition'] = [
        'type': 'literal',
        'value': definition,
        'negate': negate
      ]
    } else {
      def dl = new DLSpec(isInstance: true)
      def code = definition.rehydrate(dl, this, this)
      code.resolveStrategy = Closure.DELEGATE_ONLY
      code()
      rule['definition'] = dl.structure[0]
    }

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
