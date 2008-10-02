template<typename T1, typename T2> class subrule_list {
};

template<int T1, typename T2, typename T3> class subrule_parser {
};

template <int N, typename ListT>
struct get_subrule {
    typedef typename get_subrule<N, typename ListT::rest_t>::type type;
};

template <typename ParserT, typename ScannerT>
struct parser_result
{
    //typedef typename parser_type::template result<ScannerT>::type type;
};

template <int ID, typename ScannerT, typename ContextResultT>
struct get_subrule_result
{
            typedef typename
                get_subrule<ID, typename ScannerT::list_t>::type
            parser_t;

            typedef typename parser_result<parser_t, ScannerT>::type
            def_result_t;
};
