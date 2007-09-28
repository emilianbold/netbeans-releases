namespace boost {
    namespace threads {
        namespace mac {

            typedef enum {
                k_eExecutionContextSystemTask,
                k_eExecutionContextMPTask,
            } execution_context_t;

            execution_context_t execution_context();

            inline bool at_st()
                {    return(execution_context() == k_eExecutionContextSystemTask);}

            inline bool at_mp()
                {    return(execution_context() == k_eExecutionContextMPTask); }
            inline bool in_blue()
                {    return(!at_mp());   }


        } // namespace mac
    } // namespace threads
} // namespace boost
typedef enum {
    A1, 
    A2,
} tA;
