namespace bug256739 {
    struct AAA256739 {
        struct Iterator256739 {
            AAA256739& operator*();
            Iterator256739& operator++();
            bool operator!=(const Iterator256739 &other);
        };

        Iterator256739 begin() const;

        Iterator256739 end() const;

        void foo256739() const {
            for (const auto &var : *this) {
                var.foo256739(); // foo is unresolved
            }
        }
    };
}