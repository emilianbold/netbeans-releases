enum class Enumeration : unsigned int {
    Val1,
    Val2,
    Val3 = 100,
    Val4 /* = 101 */
};

unsigned int k = Enumeration::Val1;