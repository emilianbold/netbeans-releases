namespace {
    struct MyClass {
        int field;
    };

    template<typename T>
    struct MyIterator {

        T operator*();

        T* operator->();

        bool operator!=(MyIterator<T> &other);

        MyIterator<T> operator++();
    };

    template<typename T>
    struct MyVector {

        MyIterator<T> begin();

        MyIterator<T> end();

    };

    int foo() {
        MyVector<MyClass> vector;

        for (auto var : vector) {
            var.field;
        }
    }
}