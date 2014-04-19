namespace bug242284 {
    int mainbug242284() {
        return []() -> int {
            struct InnerStruct1 {
                int foo() { return 0; };
            }; 
            int aa = InnerStruct1().foo();

            struct InnerStruct2 {
                int boo() { return 1; };
                InnerStruct2() {};
            }; 
            int bb = InnerStruct2().boo();

            return aa + bb;
        }();            
    }
}