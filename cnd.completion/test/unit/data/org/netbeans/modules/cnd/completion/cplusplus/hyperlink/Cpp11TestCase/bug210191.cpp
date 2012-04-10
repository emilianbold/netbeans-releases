class bug210191_C {
  private:
    int m_i;

  public:
    bug210191_C() : m_i(-1) {
      [this] () -> void { m_i = 0; } ();
    }
};

class Lookup {
public:

    Lookup() {
    };

    ~Lookup();

};

Lookup::~Lookup() {

    class MySink {
    public:

        virtual void unresolved(const void *key, Lookup *as) {
            delete as;
        }
    };
    MySink mysink;
    mysink.unresolved(0,0);
}
