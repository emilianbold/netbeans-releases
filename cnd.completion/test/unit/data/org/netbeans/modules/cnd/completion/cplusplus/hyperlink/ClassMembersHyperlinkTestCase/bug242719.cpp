namespace bug242719 {
    
    struct BBB242719;

    struct AAA242719 {
        int foo(); 

        BBB242719& operator + (BBB242719 &other) {
            return other;
        }    
    };

    struct BBB242719 {
        int boo();

        AAA242719& operator + (AAA242719 &other) {
            return other;
        }    
    };

    int main(int argc, char** argv) {
        AAA242719 a;
        BBB242719 b;
        (b + a).foo(); // foo is unresolved, it suggests boo here
        (a + b).boo();
        return 0;
    } 
}