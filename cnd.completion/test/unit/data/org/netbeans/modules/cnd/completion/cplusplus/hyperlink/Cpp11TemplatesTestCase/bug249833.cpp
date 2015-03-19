namespace bug249833 {
    struct True249833 {
        constexpr static bool value = true;
    };
    struct False249833 {
        constexpr static bool value = false;
    };

    template <bool val, typename T1, typename T2> 
    struct If249833 {
        typedef T2 type;
    };

    template <typename T1, typename T2> 
    struct If249833<true, T1, T2> {
        typedef T1 type;
    };

    template <typename...Elems>
    struct And249833 {};

    template <typename Element>
    struct And249833<Element> : If249833<Element::value, True249833, False249833>::type {};

    template <typename Element, typename...Elems> 
    struct And249833<Element, Elems...> : If249833<Element::value, And249833<Elems...>, False249833>::type {};

    int main249833() {
        And249833<True249833, True249833, False249833>::value; 
        And249833<True249833, True249833, True249833>::value; 
        And249833<
                True249833, 
                True249833, 
                True249833, 
                True249833, 
                And249833<True249833, True249833, False249833>>::value; 
        And249833<
                True249833, 
                True249833, 
                And249833<True249833, True249833>, 
                And249833<True249833, True249833, True249833>>::value; 
        return 0;
    } 
}