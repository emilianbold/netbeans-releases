template <typename T> struct TypeTraits {
    enum { isHuge = (sizeof(T) > 8 ? 1 : 0) };
    enum { isLarge = (sizeof(T) > 4 ? 1 : 0) };
};

void testUnnamedEnum() {
    bool f;
    f = !TypeTraits<int>::isLarge;
    f = TypeTraits<int*>::isLarge;
    f = TypeTraits<int&>::isLarge;
}

