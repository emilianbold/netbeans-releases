template<typename K, typename V = K> class pair {
    K first;
    V second;
};

template<typename T> struct base {
    T param_t;
};

template <> struct base<int> {
    int param_int;
};

template <> struct base<pair<char, int> > {
    int param_char_int;
};

int foo() {
    // use main template
    base<char> bc;
    if (bc.param_t)
        return 0;

    // use specialization for int
    base<int> bi;
    if (bi.param_int)
        return 0;

    // use specialization for pair <char, int>
    base<pair<char, int> > bci;
    if (bci.param_char_int)
        return 0;

    return 1;
}
