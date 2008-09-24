namespace IZ148022 {

    class A {
    public:
        class B;
    private:
        class C;
    };

    class A::B {};
    class A::C {};

}
