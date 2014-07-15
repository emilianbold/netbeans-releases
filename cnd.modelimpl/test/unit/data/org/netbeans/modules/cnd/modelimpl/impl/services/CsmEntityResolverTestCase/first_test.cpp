
int foo(int param);
int foo(int *param);
int foo(int param, double x);

namespace test {
  int foo(int param);
}

struct AAA {
  int foo(int param);
};