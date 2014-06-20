namespace bug243594 {
    struct XXX243594 {
        typedef XXX243594 type;
        int foo243594();
    };

    template <typename T>
    struct AAA243594 {    
        struct BBB243594 {
            typedef T bug243594;
            typedef bug243594 alias243594;
        };
    };

    int main243594() {
        AAA243594<XXX243594>::BBB243594::alias243594 var;
        var.foo243594();
    }
}