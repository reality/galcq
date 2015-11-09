class ABoxSpec extends DLSpec {
  def instance(Closure block, var) {
    if(!isInstance) { // No instances within instances
      def rule = [:]
      def dl = new DLSpec(isInstance: true)
      def code = block.rehydrate(dl, this, this)
      code.resolveStrategy = Closure.DELEGATE_ONLY
      code()

      rule['type'] = 'instance'
      rule['definition'] = dl.structure
      rule['instance'] = var
      structure << rule
    }
  }

  def relation(Closure block, var) {
    if(!isInstance) { // No instances within instances
      def rule = [:]
      def dl = new DLSpec(isInstance: true)
      def code = block.rehydrate(dl, this, this)
      code.resolveStrategy = Closure.DELEGATE_ONLY
      code()

      rule['type'] = 'instance'
      rule['definition'] = dl.structure
      rule['variable'] = var
      structure << rule
    }
  }
}
