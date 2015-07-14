
#define __THROW
#define __CONCAT(x,y)	x ## y
#define __STRING(x)	#x

#define __MATHCALL(function,suffix, args)	\
  __MATHDECL (_Mdouble_,function,suffix, args)

#define __MATHDECL(type, function,suffix, args) \
  __MATHDECL_1(type, function,suffix, args); \
  __MATHDECL_1(type, __CONCAT(__,function),suffix, args)
#define __MATHCALLX(function,suffix, args, attrib)	\
  __MATHDECLX (_Mdouble_,function,suffix, args, attrib)
#define __MATHDECLX(type, function,suffix, args, attrib) \
  __MATHDECL_1(type, function,suffix, args) __attribute__ (attrib); \
  __MATHDECL_1(type, __CONCAT(__,function),suffix, args) __attribute__ (attrib)
#define __MATHDECL_1(type, function,suffix, args) \
  extern type __MATH_PRECNAME(function,suffix) args __THROW

#define _Mdouble_ 		double
#define __MATH_PRECNAME(name,r)	__CONCAT(name,r)

__MATHCALL (atan,, (_Mdouble_ __x));
 
__MATHCALL (tan,, (_Mdouble_ __x));
//-----------------------
//--- excerpts from boost headers

#define BOOST_PP_CAT(a, b) BOOST_PP_CAT_I(a, b)
#define BOOST_PP_CAT_I(a, b) a ## b

// this is the only macro that has its right part replaced by just "1"
// since it expands to 1
// (otherwise we have too complicated file)
#define BOOST_PP_AUTO_REC(pred, n) 1

# define BOOST_PP_REPEAT_1(c, m, d) BOOST_PP_REPEAT_1_I(c, m, d)
# define BOOST_PP_REPEAT_2(c, m, d) BOOST_PP_REPEAT_2_I(c, m, d)

# define BOOST_PP_REPEAT_1_I(c, m, d) BOOST_PP_REPEAT_1_ ## c(m, d)
# define BOOST_PP_REPEAT_2_I(c, m, d) BOOST_PP_REPEAT_2_ ## c(m, d)

# define BOOST_PP_REPEAT_1_0(m, d)
# define BOOST_PP_REPEAT_1_1(m, d) m(2, 0, d)
# define BOOST_PP_REPEAT_1_2(m, d) BOOST_PP_REPEAT_1_1(m, d) m(2, 1, d)

# define BOOST_PP_REPEAT BOOST_PP_CAT(BOOST_PP_REPEAT_, BOOST_PP_AUTO_REC(BOOST_PP_REPEAT_P, 4))

//--- below is the example of these macros usage

#define DECL(z, n, text) text ## n = n;

BOOST_PP_REPEAT(2, DECL, int x) 

//----------------
#define CAT(a, b) a ## b
#define FUN_1(x) int x  = 1;
#define MAC_A  CAT(FUN_, 1)
MAC_A(b)
//----------------
#define MAC1 A
#define MAC2 B
#define MAC1MAC2 A_B

#define MAC3(x, y) x ## y

#define MAC4(x, y) MAC3(x, y)

// this expands to A_B
int MAC3(MAC1, MAC2);

// this one expands to AB !!!
int MAC4(MAC1, MAC2); 

//----------------
#define FUN_2(x) int x  = 2;
#define MAC_B  FUN_2
MAC_B(z)
//----------------

#define EMPTY_MACRO
#define TO_EMPTY(x)         PREFIX(EMPTY_MACRO)
#define PREFIX(x)  __STRING(x)
char* buf = TO_EMPTY(1);

#define conc1(a,b) a##b##1
#define conc(a,b) conc1(b,a)
#define A
#define B
int x = conc1(,);
int y = conc(A,B);

