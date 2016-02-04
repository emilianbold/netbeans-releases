namespace bug257647 {
    
    void foo257647(int var);

    void foo257647(char *ptr);

    int main257647() {
        char *ptr1 = 0;
        char *ptr2 = 0;
        foo257647(ptr1 + 1);
        foo257647(1 + ptr1);
        foo257647(ptr1 - 1);
        foo257647(ptr2 - ptr1);
        return 0;
    }
}