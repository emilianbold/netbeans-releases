struct sta {
    __nlink_t st_nlink;        /* Link count.  */
    __mode_t st_mode;        /* File mode.  */
    /* this is a comment
    and it extends until the closing
    star-slash comment mark */
    int a;      /*f*/
    /* This comment should be ignored*/
    
    double b;   // double slash comment
    abc d;  //double slash comment2

    // Comment 1
    dfhd cd;    // Comment 2
    // Comment 3
    dfhd ef;    /*! Comment 4*/
    /* Comment 5*/
    dfhd gh;    /// Comment 6
}

sta.st_nlink;
sta.st_mode;
sta.a;
sta.b;
sta.d;
sta.cd;
sta.ef;
sta.gh;

#define MAXISIZE 500000    // maximum iterated elements allowed to written per file
#define B(x/*param*/, y /*param2*/) /*fake comment*/ BODY + x + y  // line com