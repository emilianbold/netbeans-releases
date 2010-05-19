template<> class bug186388_complex<double>
{
 public:

   double& real();

 private:
   typedef __complex__ double _ComplexT;
   _ComplexT _M_value;

 };

inline double& bug186388_complex<double>::real()
{ return __real__ _M_value; }
