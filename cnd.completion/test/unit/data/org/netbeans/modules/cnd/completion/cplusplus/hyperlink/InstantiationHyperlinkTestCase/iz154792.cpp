template <bool b> struct iz154792_E {
    typedef int eType;
};

int iz154792_main() {
    iz154792_E<(1?false:true)>::eType p; // unresolved eType
}