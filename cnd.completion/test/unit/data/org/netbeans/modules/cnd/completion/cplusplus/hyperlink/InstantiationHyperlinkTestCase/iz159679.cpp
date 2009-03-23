template <class T>
struct A159679 {
    typedef T type;
};
struct B159679 {
    typedef B159679 type;
};
int t159679;
struct C159679
{
    typedef B159679 t159679;
    typedef A159679< t159679 >::type::type type;
};
