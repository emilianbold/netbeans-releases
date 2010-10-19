typedef int (bug191083_opt_handler_t(char*));

struct {
    bug191083_opt_handler_t* handler; // <=== parser errors
    int i;
} bug191083_ST;