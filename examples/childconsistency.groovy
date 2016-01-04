println "This succeeds by building a model in which all Joe's children are the same instance (in description logics we do not assume that every symbol is unique unless explicitly stated!)"

def ontology = new Ontology()

ontology.setTBox {
  ≡ 'ParentWithMax2Children', { ≤ 2, 'HasChild', '⊤' }
}
ontology.setABox {
  relation 'HasChild', 'joe', 'ann'
  relation 'HasChild', 'joe', 'eva'
  relation 'HasChild', 'joe', 'mary'
  instance 'ParentWithMax2Children', 'joe'
}

ontology.checkConsistency()
