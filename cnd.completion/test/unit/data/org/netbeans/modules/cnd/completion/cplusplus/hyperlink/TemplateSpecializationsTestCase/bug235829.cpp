#include "bug235829.h"

namespace bug235829 {

template <>
struct container<A> {
    int foo();
};

int roo(alias d) {
    d.foo();
}

}