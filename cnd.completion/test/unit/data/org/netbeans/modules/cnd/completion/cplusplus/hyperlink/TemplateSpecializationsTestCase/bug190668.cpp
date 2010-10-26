template <class X>
class ATemplate {
public:
    ATemplate(){}
    ATemplate(const ATemplate& orig){}
    virtual ~ATemplate(){}

    void doit(); //a)  <-----

private:
    int m_a;
};

template <>
void ATemplate<int>::doit(){
    m_a = 1; //b) <-----
}