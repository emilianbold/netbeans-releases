namespace bug258511 {
  void foo258511(const char *prm) {}
  
  void foo258511(int prm) {}

  int main258511(int argc, char** argv) {
    foo258511(true ? "Gamma" : "Linear");
    return 0;
  }
}