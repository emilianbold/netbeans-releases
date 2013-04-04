#include <iostream>

using namespace std;


class a {
    int b;

public:    
    a(int val) : b(val) {
        cout << "Constructor called! Value = " << val << endl;
    }
};

void a(int val) {
    cout << "Function called! Value = " << val << endl;
}

int main(int argc, char** argv) {
    a(10);  // click here navigates to constructor
    return 0;
}