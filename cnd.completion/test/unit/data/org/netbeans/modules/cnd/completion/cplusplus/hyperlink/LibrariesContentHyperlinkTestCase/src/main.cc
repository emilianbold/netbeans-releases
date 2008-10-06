
#include <include1.h>
#include "include2.h"
#include "include.h"
int main(int argc, char** argv) {
    
    AAA aaa = new AAA(); // test hyperlink on both AAA
    
    BBB bbb = new BBB(); // test hyperlink on second BBB

    return (EXIT_SUCCESS);
}

namespace SysAlias = sys_ns;
namespace PrjAlias = prj_ns;

void foo() {
    SysAlias::string str;
    PrjAlias::Cls obj;
    std::size_t size;
}

using namespace NNN;
        
void bar() {
    nnnFoo1();
    nnnFoo2();
}        