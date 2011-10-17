void range_for_foo4() {
    int my_array[5] = {1, 2, 3, 4, 5};
    for(int &x : my_array) {
        x *= 2;
    }
}