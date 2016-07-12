namespace bug261517 {
    int main261517() {
         auto var1 = static_cast<signed char> (1) + 1;
         auto var2 = static_cast<unsigned long long> (1) + 1;
         auto var3 = static_cast<long long> (1) + 1;
         auto var4 = static_cast<wchar_t> (1) + 1;
         return 0;
    }
}
