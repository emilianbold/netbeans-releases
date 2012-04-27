template <typename T>
class Test {
  public:
    template <typename U>
    class Lib {
      public:
        static int uvec;
        void echo();
    };
};

template <typename T> template <typename U>
int Test<T>::Lib<U>::uvec(4);