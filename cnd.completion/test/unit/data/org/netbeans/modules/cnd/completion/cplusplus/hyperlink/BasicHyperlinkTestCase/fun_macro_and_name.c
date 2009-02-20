#define PREFIX(ident) normal_ ## ident

typedef struct prefix {
  const char *name;
  void *binding;
} PREFIX;

static int PREFIX(scanRef)(const int *enc)
{
    PREFIX pref;
}
