typedef struct
{
    uint i;
} SomeStruct;

void Dosomething(SomeStruct* arrayOfSomeStruct)
{
    arrayOfSomeStruct[0].i;
    arrayOfSomeStruct[0U].i;
}