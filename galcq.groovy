@Grab(group='org.apache.commons', module='commons-lang3', version='3.4')

// Merge the TBox into the ABox

// AND rule:
// Condition: A contains (C AND D)(a) but not both C(a) and D(a)
// Action: A' = A UNION { C(a), D(a) }

// OR rule:
// Condition: A contains (C OR D)(a) but neither C(a) or D(a)
// Action: A' = A UNION { C(a) } and A'' = A UNION { D(a) }

// EQ rule:
// Condition: A contains (EQr.C)(a) but there is no c with { r(a, c), C(c) } subset of A
// Action: A' = A UNION { r(a, b), C(b) } where b is a new individual name

// UQ rule:
// Condition: A contains (UQr.C)(a) and r(a, b) but not C(b)
// Action: A' = A UNION {C(b)}


// reduce to consistentcy
// normal form
// apply the rules

def ontology = new Ontology()

// TODO: In ABox only top level blocks allowed should be instance
ontology.setABox {}

ontology.setTBox {
  gci 'Woman', { and 'Person', 'Female' }

  gci 'Person', { and 'Sleepy', 'Biscuit' }

  gci 'Biscuit', { and 'Squirrel', { not 'Sleepy' } }
}

println ontology.TBox
/*
ontology.convertTBox()
ontology.printRules(ontology.ABox)
*/

def reasoner = new Reasoner(ontology)
reasoner.checkConsistency()



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
}
*/
