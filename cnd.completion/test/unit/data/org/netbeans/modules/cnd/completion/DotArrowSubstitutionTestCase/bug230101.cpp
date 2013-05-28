namespace {

    struct MyClass {
        void Test() {}
    };

    template <class T> 
    struct my_vector {
        typedef T* iterator;
    };

    template <class T>
    struct my_foreach_iterator {
        typedef typename T::iterator type;
    };

    template <class Iterator>
    struct my_iterator_reference
    {
        typedef Iterator type;
    };

    template<typename T>
    struct my_foreach_reference : my_iterator_reference<typename my_foreach_iterator<T>::type>
    {
    };

    template <class T>
    typename my_foreach_reference<T>::type myDeref() {

    }

    int main()
    {
        // here dot before Test() should be replaced with arrow
        (*myDeref<my_vector<MyClass*> >());

        return 0;
    }

}
