#  ifndef BOOST_REGEX_USER_CONFIG
#     define BOOST_REGEX_USER_CONFIG <preproc_test_include_with_macro_sys.h>
#  endif

#  include BOOST_REGEX_USER_CONFIG

#define STR(x) #x

#define INCLUDE(x) STR(x)

#include INCLUDE(preproc_test_include_with_macro.h)

#define CONCAT(x,y) x##y
#define INCLUDE_VAR(x) INCLUDE(CONCAT(x,2))
#include INCLUDE_VAR(preproc_test_include_with_macro)
