template<typename T> struct iz144079_spec {
    T field_t;
};

template <> struct iz144079_spec<int> {
    int field_int;
};

void iz144079_foo() {
    iz144079_spec<int> vi; // hyperlink should go to specialization!
    iz144079_spec<char> vc;
    vi.field_int = 0;
    vc.field_t = 'c';
}