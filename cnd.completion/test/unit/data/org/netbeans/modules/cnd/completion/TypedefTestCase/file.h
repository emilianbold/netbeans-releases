

typedef struct _ChildStruct{
    int x;
    int y;
    
} ChildStruct;


typedef struct _ParentStruct{
    ChildStruct child;
    int a;
    int b;
    _ChildStruct _child;
} ParentStruct;

