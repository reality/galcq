@Grab(group='org.apache.commons', module='commons-lang3', version='3.4')

// Merge the TBox into the ABox
// reduce to consistency
// normal form
// apply the rules

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

//ontology.expandABox()
//ontology.printRules(ontology.ABox)
ontology.checkConsistency()

/*def reasoner = new Reasoner(ontology)

println reasoner.checkConsistency()
*/

/*ontology.setTBox {
  ⊑ 'Woman', { ⊓ 'Person', 'Female' }

  ⊑ 'Person', { ⊓ 'Sleepy', 'Biscuit' }

  ⊑ 'Biscuit', { ⊓ 'Squirrel', { not 'Sleepy' } }
}*/

/** 3.1*/
/*ontology.setTBox {
  ⊑ ({ ∀ 'r', { ∀ 's', { ⊓ 'A', { ∃ 'r', { ∀ 's', { ⊓ 'B', { ∀ 'r', { ∃ 's', 'C' } } } } } } } },
     { ∃ 'r', { ∃ 's', { ⊓ 'A', { ⊓ 'B', 'C' } } } })
}*/

/*
ontology.setTBox {
  ⊑ ({ ∀ 'r', { ∀ 's', { ⊓ 'A', { ⊔({ ∃ 'r', { ∀ 's', { not 'A' } } }, { ∀ 'r', { ∃ 's', 'B' } }) } } } },
     { ∀ 'r', { ∃ 's', { ⊔({ ⊓ 'A', 'B' }, { ∃ 'r', { ∀ 's', { not 'B' } } }) } } })
}
*/

  // Condition: A contains LTEnr.C(a), and there are b1,..,bn+1 with { r(a, b1), C(b1), ... r(a,bn+1), C(bn+1) } but NO { bi != bj | 1 <= i <= n, 1 <=j <= n, i != j}
  /*
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
*/



/*
ontology.setTBox {
  gci {
    all 'r', { 
      all 's', {
        and 'A', {
          some 'r', {
            all 's', {
              and 'B', {
                all 'r', {
                  some 's', 'C'
                }
              }
            }
          }
        }
      } 
    }
  }, {
    some 'r' {
      some 's' {
        and 'A' {
          and 'B', 'C' 
        }
      }
    }
  }
}

ontology.convertTBox()
println ontology.ABox

//ontology.checkConsistency()


ontology.setABox {
  or 'A', {
    and 'C', { gt 3, 'hasSquirrel', 'B' }
  }
  all 'hasSibling', {
    or 'Female', 'Male' 
  }
  gt 2, 'hasSibling', 'Biscuit'
  instance , 'A'
}
*/
