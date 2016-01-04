def ontology = new Ontology()

ontology.setTBox {
  ⊑ ({ ∀ 'r', { ∀ 's', { ⊓ 'A', { ∃ 'r', { ∀ 's', { ⊓ 'B', { ∀ 'r', { ∃ 's', 'C' } } } } } } } },
     { ∃ 'r', { ∃ 's', { ⊓ 'A', { ⊓ 'B', 'C' } } } })
}

ontology.checkSubsumption()
