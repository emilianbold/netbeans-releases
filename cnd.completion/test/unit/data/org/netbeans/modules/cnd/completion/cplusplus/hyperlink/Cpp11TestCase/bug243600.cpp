namespace bug243600 {
    enum class myEnum_243600 : int {
        A243600, B243600, C243600
    };

    struct foo_243600 {
        int field_243600;
    };

    int boo_243600()
    {
        foo_243600 arr[] = { 100, 200, 400 };

        int a = arr[ 0 ].field_243600; // OK
        int b = arr[ (int) 0  ].field_243600; // OK
        int c = arr[ static_cast<int>(myEnum_243600::A243600) ].field_243600; //  field highlighted as unable to resolve 
        int d = arr[ static_cast<long>(0) ].field_243600;        //  field highlighted as unable to resolve

        return 0;
    }   
}