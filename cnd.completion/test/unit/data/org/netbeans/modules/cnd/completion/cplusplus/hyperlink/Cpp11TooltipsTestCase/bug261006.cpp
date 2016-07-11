namespace bug261006 {
    struct AAA261006 {
        AAA261006(int var) {}
        AAA261006(double var) {}
    };

    AAA261006 operator+(AAA261006 x, int y) {
        return AAA261006(y);
    }

    int main261006(int argc, char** argv) {
        auto var1 = 1 + 2;
        auto var2 = AAA261006(1) + 2;
        return 0;
    }
}