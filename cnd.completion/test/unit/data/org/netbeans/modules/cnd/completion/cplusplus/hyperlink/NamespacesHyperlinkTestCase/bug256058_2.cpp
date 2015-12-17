namespace bug256058_2 {
    namespace outer256058_2 {
        namespace inner256058_2 {}

        using namespace inner256058_2;

        namespace inner256058_2 {
            struct AAA256058_2 {
                int field;
            };
        }
    } 

    int main256058_2() {
        outer256058_2::AAA256058_2 var;
        var.field = 0;
        return 0;
    } 
}