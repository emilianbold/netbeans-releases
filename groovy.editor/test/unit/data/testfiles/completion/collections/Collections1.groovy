def rangeTest() {
    // "groovy.lang.Range"
    // this is buggy in test case
    (1..10).a
    // this should offer cc for integer
    1..10.d
}

def listTest() {
    // "java.util.List"
   ["one","two"].listIter
}

def mapTest() {
    // "java.util.Map"
  [1:"one", 2:"two"].ent
}