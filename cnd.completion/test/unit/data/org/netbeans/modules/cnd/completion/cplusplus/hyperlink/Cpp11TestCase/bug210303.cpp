template<typename... Values>
class bug210303_tuple;

template<> class bug210303_tuple<> { };

template<typename Head, typename... Tail>
class bug210303_tuple<Head, Tail...> 
  : private bug210303_tuple<Tail...>
{
    public:
        int i;
};

template<typename Head, typename Values> class bug210303_A {

    void foo() {
        bug210303_tuple<Head, Values...>& t;
        t.i;
    }
};
