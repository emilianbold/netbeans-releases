namespace IZ145286 {
    void foo() {
        if (typename int ci1 = 1) {
            ci1;
        }
        while (volatile int ci2 = 1) {
            ci2;
        }
        switch (const int ci3 = 1) {
            case 1: ci3;
        }
    }
}
