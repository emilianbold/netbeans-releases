class table191457  {
public:
    int i;

    class array {
    public:
        int data;
        int top;
        array(const char* label = 0, int max = 0);
    };
};

typedef table191457::array arr191457;

arr191457::array(const char* label, int max)
    : data(0)
    , top(0)
{
   
}
