class Cc {};
class Dd {}; //this must not show is completion dialog for ns::
int func() {}
char* buf;
double &number;

namespace ns { 
    using ::Cc;
    using ::Ee;
    using ::func;
    using ::buf;
    using ::number;
}

int main() {
    // completion test is performed here
    return 0;
}

namespace ns2 {
    //completion test is performed here
}

// this goes at the end of file to make completion task a bit harder
enum Ee { AA, BB };
