namespace {
  class PD {
    friend struct A;
  public:
    typedef int nn;
  };
  struct DD {
    typedef int nn;
  };

  struct A {
    decltype(PD()) s; // ok
    decltype(PD())::nn n; // ok
    decltype(DD()) *p = new decltype(DD()); // ok
  };

  decltype(((13, ((DD())))))::nn dd_parens; // ok
  decltype(((((42)), PD())))::nn pd_parens_comma; // ok
}
