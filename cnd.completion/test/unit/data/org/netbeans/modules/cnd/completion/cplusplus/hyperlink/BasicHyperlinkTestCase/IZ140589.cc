#define LONG(x) long x
template<class T> class C140589 {
    static const int value = 1;
};
int f140589() {
    return 2;
}
int result1 = C140589<LONG(int)>::value;
int result2 = C140589<f140589()>::value;
