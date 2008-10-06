namespace std {
    typedef unsigned int size_t;
}


#define BOOST_TT_AUX_TYPE_TRAIT_PARTIAL_SPEC1_2(param1,param2,trait,spec,result) \
template< param1, param2 > struct trait<spec> \
{ \
    typedef result; \
}; \
/**/

#   define BOOST_MPL_AUX_LAMBDA_SUPPORT(i,name,params) /**/
#define BOOST_TT_AUX_TEMPLATE_ARITY_SPEC(i, name) /**/

#define BOOST_TT_AUX_TYPE_TRAIT_DEF1(trait,T,result) \
template< typename T > struct trait \
{ \
    typedef result type; \
    BOOST_MPL_AUX_LAMBDA_SUPPORT(1,trait,(T)) \
}; \
\
BOOST_TT_AUX_TEMPLATE_ARITY_SPEC(1,trait) \
/**/


#define BOOST_TT_AUX_TYPE_TRAIT_PARTIAL_SPEC1_1(param,trait,spec,result) \
template< param > struct trait<spec> \
{ \
    typedef result type; \
}; \




namespace boost {

BOOST_TT_AUX_TYPE_TRAIT_DEF1(remove_all_extents,T,T)

BOOST_TT_AUX_TYPE_TRAIT_PARTIAL_SPEC1_2(typename T,std::size_t N,remove_all_extents,T[N],typename boost::remove_all_extents<T>::type type)
BOOST_TT_AUX_TYPE_TRAIT_PARTIAL_SPEC1_2(typename T,std::size_t N,remove_all_extents,T const[N],typename boost::remove_all_extents<T const>::type type)

BOOST_TT_AUX_TYPE_TRAIT_PARTIAL_SPEC1_1(typename T,remove_all_extents,T[],typename boost::remove_all_extents<T>::type)
BOOST_TT_AUX_TYPE_TRAIT_PARTIAL_SPEC1_1(typename T,remove_all_extents,T const[],typename boost::remove_all_extents<T const>::type)

}
