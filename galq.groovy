class DLSpec {
  def top

  def and(Object[] args) { buildJunction('⊓', args) }
  def or(Object[] args) { buildJunction('⊔', args) }

  def gt(amt, r, Object[] args) { buildQuantifier('≥', r, amt, args) }
  def lt(amt, r, Object[] args) { buildQuantifier('≤', r, amt, args) }

  def some(r, Object[] args) { buildQuantifier('∃', r, null, args) }
  def all(r, Object[] args) { buildQuantifier('∀', r, null, args) }

  private buildQuantifier(String quantifier, String r, amt, Object[] args) {
    def first = true

    if(amt) {
      print "$quantifier$amt $r."
    } else {
      print "$quantifier$r."
    }

    args.each { x ->
      if(x instanceof String) {
        print x
      } else if(x instanceof Closure) {
        def dl = new DLSpec()
        def code = x.rehydrate(dl, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        print '('
        code()
        print ')'
      }
      first = false
    }

    if(top) {
      println ''
    }
  }

  private buildJunction(String junction, Object[] args) {
    def first = true

    args.each { x ->
      if(!first) {
        print " $junction "
      }
      if(x instanceof String) {
        print x
      } else if(x instanceof Closure) {
        def dl = new DLSpec()
        def code = x.rehydrate(dl, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        print '('
        code()
        print ')'
      }
      first = false
    }

    if(top) {
      println ''
    }
  }
}

def ABox(@DelegatesTo(DLSpec) Closure cl) {
    def dl = new DLSpec(top: true)
    def code = cl.rehydrate(dl, this, this)
    code.resolveStrategy = Closure.DELEGATE_ONLY
    code()
}

ABox {
  or 'A', {
    and 'C', { gt 3, 'hasSquirrel', 'B' }
  }
  all 'hasSibling', {
    or 'Female', 'Male' 
  }
  gt 2, 'hasSibling', 'Biscuit'
}
