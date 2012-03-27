template<int A, int B> struct point;
template<int AT, int... BT> struct points<point<AT, BT>...> {};
