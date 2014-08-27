namespace bug246349 {
  struct Holder246349 {
      template <typename T>
      Holder246349(T &&t) {}
  };

  struct Wrapper246349 {
      Holder246349 holder;
      Wrapper246349() : holder([](){int unresoved; return unresoved;}) {};
  };
}