// This succeeds by building a model in which all John's children are the same instance (in description logics we do not assume that every symbol is unique unless explicitly stated!)

def ontology = new Ontology()

ontology.setABox {
  instance ({ ≤ 3, 'hasChild', '⊤' }, 'John')
  relation 'hasChild', 'John', 'A'
  relation 'hasChild', 'John', 'B'
  relation 'hasChild', 'John', 'C'
  relation 'hasChild', 'John', 'D'
  instance 'John', 'B'
  instance 'John', 'C'
  instance 'John', 'D'
  instance 'John', 'E'
}

ontology.checkSubsumption()
