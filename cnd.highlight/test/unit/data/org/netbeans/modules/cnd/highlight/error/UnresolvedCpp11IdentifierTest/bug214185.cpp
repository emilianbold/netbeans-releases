template<class Container>
inline auto cbegin(Container &cont) -> decltype(cont)
{ return cont; }

