struct {
  char bug189039_charfield;
  enum {
    bug189039_ALTERNATIVE_1,
    bug189039_ALTERNATIVE_2,
  } bug189039_enumfield;

} bug189039_globalstruct;

void bug189039_testfunc() {
  bug189039_globalstruct.bug189039_enumfield = bug189039_ALTERNATIVE_1;
}