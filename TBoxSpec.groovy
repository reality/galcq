class TBoxSpec extends DLSpec {
  def gci(Closure a, Closure b) {
    def rule = [:]
    [a, b].eachWithIndex { block, i ->
      def dl = new DLSpec(isInstance: true)
      def code = block.rehydrate(dl, this, this)
      code.resolveStrategy = Closure.DELEGATE_ONLY
      code()
      rule[i] = dl.structure
    }

    rule['type'] = 'gci'

    structure << rule
  }
}
