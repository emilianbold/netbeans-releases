namespace IZ144360 {

    class AAA {};

    class BBB {
    public:
        typedef int III;
    };

    class C {
    public:
        typedef BBB AAA;
        AAA::III iii;
    };

}
