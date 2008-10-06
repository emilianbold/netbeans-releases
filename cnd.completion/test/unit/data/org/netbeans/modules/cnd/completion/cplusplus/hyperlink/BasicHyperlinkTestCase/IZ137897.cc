int (*funptr)(int funptrarg1, char* funptrarg2);
int (*funptr2)(int (*funptrarg1)(char funptrarg2));
typedef int (*funptr3)(int funptrarg1);
