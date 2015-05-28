namespace bug252597 {
  template <typename T>
  struct Next252597 {
    typedef typename T::next next;
  };

  struct First252597 {
    typedef int next;
  };

  struct Second252597 {
    typedef First252597 next;
  };

  template <typename T>
  struct AAA252597 : AAA252597<typename Next252597<T>::next> {};

  template <>
  struct AAA252597<int> {
    int finish252597();
  };

  void foo252597() {
    AAA252597<Second252597> var;
    var.finish252597(); // finish is unresolved
  }
}