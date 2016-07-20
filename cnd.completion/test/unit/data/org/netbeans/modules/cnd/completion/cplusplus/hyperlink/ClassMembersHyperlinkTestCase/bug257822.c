struct Foo257822
{
    int foo1; 
};

void *baz1_257822 = &(struct Foo257822)
{
    .foo1 = 0,  // .foo1 is NOT resolved
}; 
   
struct Bar257822
{ 
    int foo1;
};

struct Bar257822 *baz3_257822 = (void *)&(struct Foo257822)
{
    .foo1 = 0,  // .foo1 is resolved
};

void *baz4_257822 = (void *)&(struct Foo257822)
{
    .foo1 = 0,  // .foo1 is NOT resolved
}; 