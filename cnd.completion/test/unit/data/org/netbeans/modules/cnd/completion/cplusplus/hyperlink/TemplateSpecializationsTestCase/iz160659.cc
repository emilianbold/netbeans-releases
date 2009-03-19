class iz160659_NullType {};

template <class TList> struct iz160659_Length;

template <> struct iz160659_Length<iz160659_NullType>
{
    enum { value = 0 };
};

int iz160659_foo() {
     iz160659_Length<iz160659_NullType>::value;
}