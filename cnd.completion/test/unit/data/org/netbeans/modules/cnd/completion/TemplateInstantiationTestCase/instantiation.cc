#include "instantiation.h"

typedef MYmap<A, B> mymap;
mymap freq;
mymap::iterator iter;

void main() {
   freq.key_BAD().foo();
   freq.tp_BAD().boo();
   freq.td_key_BAD().foo();
   freq.td_pair_BAD().getKey().foo();
   freq.td_pair_BAD().getValue().boo();
   iter->key_OK();
}
