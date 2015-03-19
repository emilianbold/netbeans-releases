namespace bug251305 {
    template <typename T>
    struct AAA251305 {
        auto fun251305(AAA251305<T> var) -> decltype(var.fun251305());
    }; 
}