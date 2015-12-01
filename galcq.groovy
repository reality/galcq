@Grab(group='org.apache.commons', module='commons-lang3', version='3.4')

// Merge the TBox into the ABox
// reduce to consistency
// normal form
// apply the rules

def ontology = new Ontology()

ontology.setABox {}

/*ontology.setTBox {
  ⊑ 'Woman', { ⊓ 'Person', 'Female' }

  ⊑ 'Person', { ⊓ 'Sleepy', 'Biscuit' }

  ⊑ 'Biscuit', { ⊓ 'Squirrel', { not 'Sleepy' } }
}*/

ontology.setTBox {
  ⊑ ({ ∀ 'r', { ∀ 's', { ⊓ 'A', { ∃ 'r', { ∀ 's', { ⊓ 'B', { ∀ 'r', { ∃ 's', 'C' } } } } } } } }, 
     { ∃ 'r', { ∃ 's', { ⊓ 'A', { ⊓ 'B', 'C' } } } })
}

def reasoner = new Reasoner(ontology)
reasoner.checkSubsumption()


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
