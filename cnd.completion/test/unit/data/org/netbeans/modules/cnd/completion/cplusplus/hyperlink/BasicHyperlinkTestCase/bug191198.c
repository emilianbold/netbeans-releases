typedef struct F191198{
        int bits;
        int explicit;
} f_t191198;

f_t191198
fset_init191198()
{
        return (f_t191198) { .bits = 0, .explicit = 0,}; 
}

f_t191198*
fset_init191198_ptr()
{
        return &(f_t191198) { .bits = 0, .explicit = 0,}; 
}

f_t191198
fset_init191198_init()
{
    f_t191198 *b, *c;
    *b = (f_t191198) { .bits = 0, .explicit = 0,}; 
    c = &(f_t191198) { .bits = 0, .explicit = 0,}; 
    return *c;
}
