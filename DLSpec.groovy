class DLSpec {
  def structure = []
  def isInstance

  def and(Object[] args) { buildJunction('⊓', args); }
  def or(Object[] args) { buildJunction('⊔', args); }

  def gt(amt, r, Object[] args) { buildQuantifier('≥', r, amt, args) }
  def lt(amt, r, Object[] args) { buildQuantifier('≤', r, amt, args) }

  def some(r, Object[] args) { buildQuantifier('∃', r, null, args); }
  def all(r, Object[] args) { buildQuantifier('∀', r, null, args) }

  private buildQuantifier(String quantifier, String r, amt, Object[] args) {
    def rule = [:]
    rule['operation'] = quantifier
    if(amt) {
      rule['amount'] = amt
    }

    args.eachWithIndex { x, i ->
      if(x instanceof String) {
        rule[i] = x
      } else if(x instanceof Closure) {
        def dl = new DLSpec()
        def code = x.rehydrate(dl, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        rule[i] = dl.structure
      }
    }

    structure << rule
  }

  private buildJunction(String junction, Object[] args) {
    def rule = [:]
    rule['operation'] = junction

    args.eachWithIndex { x, i ->
      if(x instanceof String) {
        rule[i] = x
      } else if(x instanceof Closure) {
        def dl = new DLSpec()
        def code = x.rehydrate(dl, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        rule[i] = dl.structure
      }
    }
    
    structure << rule
  }
}
