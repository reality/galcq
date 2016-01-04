// This will fail the subsumption test, since a person is defined as one who is sleepy and one who is a biscuit. a biscuit is defined as one who is a squirrel and not sleepy. an obvious contradiction.

def ontology = new Ontology()

ontology.setTBox {
  ⊑ 'Woman', { ⊓ 'Person', 'Female' }

  ⊑ 'Person', { ⊓ 'Sleepy', 'Biscuit' }

  ⊑ 'Biscuit', { ⊓ 'Squirrel', { not 'Sleepy' } }
}

ontology.checkSubsumption()
