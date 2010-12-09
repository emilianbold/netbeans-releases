#define bug190885_TEST(name) class bug190885_Test##name { virtual void RunImpl(); } test##name##Instance; void bug190885_Test##name::RunImpl()

bug190885_TEST(basic) {    
    int var;
};
bug190885_TEST(name2) {
    int var2;   //<-------Unable to resolve indentifier 'var'
};