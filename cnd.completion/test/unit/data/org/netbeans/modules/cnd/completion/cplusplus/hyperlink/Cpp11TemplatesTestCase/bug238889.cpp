namespace bug238889 {
    namespace ZZZ238889 {
        template <typename T1>
        struct XXX238889 {
            template <typename T2>
            struct BBB238889 {
            };
        };
    }

    template <typename T>
    struct AAA238889 {
        typedef int type;
    };

    int foo238889() {
        AAA238889<ZZZ238889::XXX238889<int>::BBB238889<int>>::type var1;
        AAA238889<ZZZ238889::XXX238889<int>::BBB238889<ZZZ238889::XXX238889<int>>>::type var2;
    } 
}