namespace bug243598 {
    int foo_243598()
    {
        thread_local int var = 10;
        return var + 1;
    }
}