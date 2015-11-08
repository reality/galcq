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


