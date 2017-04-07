namespace bug268930 {
    typedef unsigned long int my_size_t;

    struct AAA268930 {
        AAA268930(const char *str);
        const char* c_str();
    };

    AAA268930 operator "" _my_str(const char * str, my_size_t length) {
        return AAA268930(str);
    }

    unsigned long long int operator "" _km(unsigned long long int v) {
        return v;
    }

    template <char...Chars>
    unsigned long long int operator "" _km_raw() {
        return 0;
    }

    char operator "" _km_char(char v) {
        return v;
    } 

    long double operator "" _my_float(long double param) {
        return param;
    }

    long double operator "" _my_float_raw(const char *param) {
        return 0;
    }

    char operator "" _chr(char v) {
        return v;
    } 

    wchar_t operator "" _wchr(wchar_t v) {
        return v;
    } 

    void foo268930(const char*);
    void foo268930(AAA268930);

    int main268930() {
        'A'_chr;
        L'A'_wchr;
        int _km = 1;
        _km = 123_km + 1; 
        _km = 123_km_raw + 1; 
        _km = 'A'_km_char + 1; 
        float _my_float = 0;
        _my_float = 1.0_my_float;
        _my_float = 1.0_my_float_raw;
        foo268930("bla");
        foo268930("bla"_my_str);
        foo268930("bla"_my_str.c_str());
        return 0;
    }
}