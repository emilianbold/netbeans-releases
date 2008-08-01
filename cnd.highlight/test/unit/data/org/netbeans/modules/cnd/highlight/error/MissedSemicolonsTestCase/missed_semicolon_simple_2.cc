template<typename T> class Wrapper {
    T value;
public:
    T get();
    void set(const T&);
};

template<typename T> T Wrapper<T>::get() {
    return value;
}

template<typename T> void Wrapper<T>::set(const T& value) {
    this->value = value;
}
