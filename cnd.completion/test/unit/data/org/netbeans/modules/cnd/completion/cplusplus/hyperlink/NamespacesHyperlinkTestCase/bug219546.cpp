
namespace bug219546_a { namespace c {

    
    
class X {
public:
    virtual void m() = 0;
};

}}

namespace bug219546_a { namespace d {

using namespace ::bug219546_a::c;

void f(X *p)
{
    p->m();
}

}}