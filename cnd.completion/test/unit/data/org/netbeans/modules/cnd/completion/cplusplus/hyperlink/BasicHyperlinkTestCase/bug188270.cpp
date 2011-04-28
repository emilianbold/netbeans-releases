typedef struct {
    int a;
    char b;
} bug188270_inner;

typedef struct {
    bug188270_inner i;
} bug188270_outer;

int bug188270_main(void) {
    bug188270_outer x = {
      .i = {
          .a=5,  /* Unable to resolve identifier a */
          .b='c' /* Unable to resolve identifier b */
      }
    };

    return 0;
}