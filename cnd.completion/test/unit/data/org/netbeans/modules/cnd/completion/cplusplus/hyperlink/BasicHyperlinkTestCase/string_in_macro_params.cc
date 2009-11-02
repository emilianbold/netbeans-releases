#define string_in_macro_params_XMST(OBJ) CONST_CAST(char *,OBJ)
#define DDD_NAME     "DDD"
#define DDD_VERSION  "3.3.11"
#define DDD_HOST   "x86_64-unknown-linux-gnu"

void string_in_macro_params_foo() {
    char * s = string_in_macro_params_XMST(
		    "GNU " DDD_NAME " " DDD_VERSION " (" DDD_HOST "), "
		    "by Dorothea L\374tkehaus and Andreas Zeller.\n"
		    "Copyright \251 1995-1999 "
		    "Technische Universit\344t Braunschweig, Germany.\n"
		    "Copyright \251 1999-2001 "
		    "Universit\344t Passau, Germany.\n"
		    "Copyright \251 2001 "
		    "Universit\344t des Saarlandes, Germany.\n"
		    "Copyright \251 2001-2004 "
		    "Free Software Foundation, Inc.\n");
}