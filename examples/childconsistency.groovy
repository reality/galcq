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
