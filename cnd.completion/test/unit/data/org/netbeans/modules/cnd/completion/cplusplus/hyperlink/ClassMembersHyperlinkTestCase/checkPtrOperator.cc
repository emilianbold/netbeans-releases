namespace checkPtrOperator {
    class ReferenceType {
    public:
        void foo();
    };

    class MainType {
    public:
        ReferenceType operator*();
    public:
        int member;
    };

    void checkDeref( ) {
        MainType aaa;
        &aaa.member;
    }
}