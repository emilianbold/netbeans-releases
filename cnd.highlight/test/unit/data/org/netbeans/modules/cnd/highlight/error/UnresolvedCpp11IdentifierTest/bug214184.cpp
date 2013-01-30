class CTypeInteger {
public:
  CTypeInteger *clone() const override { return new CTypeInteger(*this); }
};