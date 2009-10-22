int
IZ175123_main ()
{
    if (int const * p = 0) // Parse error: "unable to resolve identifier"
    {
    }
    return (0);
}