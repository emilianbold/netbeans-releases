const static char global_var246684 = 1;

namespace bug246684 {
    void foo246684(int var) {
        foo246684(::global_var246684);
        foo246684((int)::global_var246684);
        int local_var = (int)::global_var246684;
    }  
}