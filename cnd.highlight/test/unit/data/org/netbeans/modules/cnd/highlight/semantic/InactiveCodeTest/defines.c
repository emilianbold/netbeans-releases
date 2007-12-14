#define DEF
#undef NDEF

#ifdef NDEF
    int def;
#else
#   ifdef DEF
#       define DEFIFDEF    
        int defifnndef;
#   else
        int ifnndef;
#   endif
#endif

#ifdef DEFIFDEF
    int defifdef;
#endif

#undef DEF
#undef DEFIFDEF

#ifdef NDEF
    int def2;
#else
#   ifdef DEF
#       define DEFIFDEF    
        int defifnndef2;
#   else
        int ifnndef2;
#   endif
#endif

#ifdef DEFIFDEF
    int defifdef2;
#endif
