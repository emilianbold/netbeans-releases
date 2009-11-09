namespace iz175231_std1 {

    template<class charT, class traits>
    class basic_ios1 {
        inline operator void*() const;
        inline bool fail() const;
    };

    template<class charT, class traits>
    basic_ios1<charT, traits>::operator void*() const {
        return fail() ? (void*) 0 : (void*) 1; // <<<== fail is unresolved
    }

    template<class charT, class traits>
    inline bool
    basic_ios1<charT, traits>::fail() const {
        return true;
    }
}
