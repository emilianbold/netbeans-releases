namespace bug257031 {
    enum A257031 { A1 = 0, A2 };

    namespace ns257031 {
        int func();
        int cmp(int a, int b);
    }

    void foo257031() {
        ns257031::func() < A1 ? A1 : A2;
    }
    
    void boo257031() {
        int s1 = 0, s2 = 1;
        int s3 = ns257031::cmp(s1, s2) < 0 ? s1 : s2;
    }
}