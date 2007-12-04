class Exception {};

namespace ns {
    class Throwable {};
}

void foo_1() throw (const char) {
}

void foo_2() throw (const char&) {
}

void foo_3() throw (Exception) {
}

void foo_4() throw (const Exception&) {
}

void foo_5() throw (const ns::Throwable&) {
}
