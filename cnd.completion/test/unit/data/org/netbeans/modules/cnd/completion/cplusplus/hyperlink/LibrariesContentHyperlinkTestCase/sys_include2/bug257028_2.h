namespace bug257028 {
    namespace outer257028 {
        namespace somens257028 {
            struct AAA257028 {
                int foo();
            };
        }

        namespace inner257028 {
            typedef typename somens257028::AAA257028 type257028;
        }

        struct BBB257028 {
            type257028 boo();
        };
    }
}