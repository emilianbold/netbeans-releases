#include "resolver_class_string.h"

std::wstring write(string str)
{
    using std::wstring;
    wstring out;
    return out;
}

namespace A {
    string read() {
        string str;
        return str;
    }

}

using std::wstring;

wstring ClassA::read() const {    
    wstring str;
    return str;
}

