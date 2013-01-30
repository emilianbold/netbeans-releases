class bug217994_String {
public:
    void c_str() {
    }
};

class bug217994_StringClass
{
    public:
        void GetString( char* str , unsigned int size, const char* format_str = "FORMAT" ) const;
        bug217994_String GetString( const char* format_str = "FORMAT" ) const;
};

int bug217994_main()
{
    bug217994_StringClass stringClass;
    stringClass.GetString().c_str();
    return 0;
}