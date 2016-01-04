// In this case we will see a failure after the first AND split because we can't generate any relations for the universal quantifiers

def ontology = new Ontology()

ontology.setTBox {
  ⊑ ({ ∀ 'r', { ∀ 's', { ⊓ 'A', { ⊔({ ∃ 'r', { ∀ 's', { not 'A' } } }, { ∀ 'r', { ∃ 's', 'B' } }) } } } },
     { ∀ 'r', { ∃ 's', { ⊔({ ⊓ 'A', 'B' }, { ∃ 'r', { ∀ 's', { not 'B' } } }) } } })
}

ontology.checkSubsumption()
