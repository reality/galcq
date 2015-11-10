class DLSpec {
  def structure = []
  def isInstance
  def negate = false

  def and(Object[] args) { buildJunction('⊓', args); }
  def or(Object[] args) { buildJunction('⊔', args); }

  def gt(amt, r, Object[] args) { buildQuantifier('≥', r, amt, args) }
  def lt(amt, r, Object[] args) { buildQuantifier('≤', r, amt, args) }

  def some(r, Object[] args) { buildQuantifier('∃', r, null, args); }
  def all(r, Object[] args) { buildQuantifier('∀', r, null, args) }

  def not(Object arg) {
    if(arg instanceof String) {
      structure << [
        'type': 'literal',
        'value': arg,
        'negate': true
      ]
    } else {
      def dl = new DLSpec(negate:true)
      def code = arg.rehydrate(dl, this, this)
      code.resolveStrategy = Closure.DELEGATE_ONLY
      code()
      structure = dl.structure
    }
  }

  private buildQuantifier(String quantifier, String r, amt, Object[] args) {
    def rule = [:]
    rule['type'] = 'operation'
    rule['operation'] = quantifier
    if(amt) {
      rule['amount'] = amt
    }

    ['left':args[0], 'right':args[1]].each { i, x ->
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

  private buildJunction(String junction, Object[] args) {
    def rule = [:]
    rule['type'] = 'operation'
    rule['operation'] = junction

    ['left':args[0], 'right':args[1]].each { i, x ->
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
