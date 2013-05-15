
namespace A {
    int elem = 1;
}


int main(int argc, char** argv) 
{
    namespace alias1 = A;
    namespace alias2 = alias1;
    
    std::cout << "elem = " << alias2::elem << std::endl;
    
    return 0;
}