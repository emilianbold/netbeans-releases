namespace bug247031 {
    struct AAA247031 {
        const int value;
        AAA247031(int val) : value(val) {};
        int getFromAAA() {return value;};
    };

    struct BBB247031 {
        const int value;
        BBB247031(int val) : value(val) {};
        int getFromBBB() {return value;};
    };

    namespace myns247031 {
        struct CCC247031 {
            CCC247031(int val);
            int getFromCCC();
        };
    }

    template <typename T>
    struct EEE247031 {
        EEE247031();
        EEE247031(T params...);
        T get();
    };

    AAA247031 func247031(AAA247031 a, int p) {
        return a;
    }

    BBB247031 func247031(BBB247031 b, float p) {
        return b;
    }

    AAA247031 funcWithParams247031(AAA247031 a, int ip1, BBB247031 b, int ip2) {
        return {ip1 + ip2};
    } 

    AAA247031 boo247031() {
        int x{};
        auto var = AAA247031{x};
        auto retVal = AAA247031{x}.getFromAAA();
        var.getFromAAA();
        func247031({x}, 1).getFromAAA(); 
        func247031({x}, 1.0f).getFromBBB();
        funcWithParams247031({x + 3}, x, {AAA247031{1}.getFromAAA() + x}, 5).getFromAAA();
        AAA247031{x}.getFromAAA(); 
        (AAA247031){x}.getFromAAA(); 
        auto scopedVar = myns247031::CCC247031{x};
        scopedVar.getFromCCC();
        myns247031::CCC247031{x}.getFromCCC();
        auto tpl = EEE247031<AAA247031>{};
        tpl.get().getFromAAA();
        EEE247031<BBB247031>{}.get().getFromBBB();
        EEE247031<const char *>{"one", "two"}.get();
        return AAA247031{x}.getFromAAA(); 
    }
}
