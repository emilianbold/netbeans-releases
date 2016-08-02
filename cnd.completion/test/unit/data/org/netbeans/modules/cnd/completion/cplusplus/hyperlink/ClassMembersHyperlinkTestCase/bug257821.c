struct Foo257821 {
    int foo1;
};

struct Bar257821 {
    int bar1;
    int bar2;
};

struct Bar257821 baz2_257821 =
{
    .bar1 = sizeof (struct Foo257821),
    .bar2 = 0,  // .bar2 is NOT resolved
};

void *baz3_257821 = (struct Foo257821[])
{
    { 
        .foo1 = 0,  // .foo1 is NOT resolved
    }, 
}; 
