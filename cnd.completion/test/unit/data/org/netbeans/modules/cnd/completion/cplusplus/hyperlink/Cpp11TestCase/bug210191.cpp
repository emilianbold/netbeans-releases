class bug210191_C {
  private:
    int m_i;

  public:
    bug210191_C() : m_i(-1) {
      [this] () -> void { m_i = 0; } ();
    }
};