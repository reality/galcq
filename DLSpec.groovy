class DLSpec {
  def structure = []
  def isInstance
  def negate = false

  def ⊓(Object[] args) { buildJunction('⊓', args) }
  def and(Object[] args) { ⊓(args) }

  def ⊔(Object[] args) { buildJunction('⊔', args) }
  def or(Object[] args) { ⊔(args) }

  def ≥(amt, r, Object[] args) { buildQuantifier('≥', r, amt, args) }
  def gt(amt, r, Object[] args) { ≥(amt, r, args) }

  def ≤(amt, r, Object[] args) { buildQuantifier('≤', r, amt, args) }
  def lt(amt, r, Object[] args) { ≤(amt, r, args) }

  def ∃(r, Object[] args) { buildQuantifier('∃', r, null, args); }
  def eq(r, Object[] args) { ∃(r, args) }

  def ∀(r, Object[] args) { buildQuantifier('∀', r, null, args) }
  def uq(r, Object[] args) { ∀(r, args) }

  // Apparently we're not allowed to use ¬ as a function name. 
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

  private buildQuantifier(String quantifier, Object r, amt, Object[] args) {
    def rule = [:]
    rule['type'] = 'operation'
    rule['operation'] = quantifier

    if(r instanceof String) {
      rule['relation'] = r
    } else {
      rule['amount'] = r
    }

    if(amt) {
      rule['amount'] = amt
    }

    def definition = args[0]
    if(definition instanceof String) {
      rule['definition'] = [
        'type': 'literal',
        'value': definition,
        'negate': negate
      ]
    } else if(definition instanceof Closure) {
      def dl = new DLSpec(negate:negate)
      def code = definition.rehydrate(dl, this, this)
      code.resolveStrategy = Closure.DELEGATE_ONLY
      code()
      rule['definition'] = dl.structure[0]
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
