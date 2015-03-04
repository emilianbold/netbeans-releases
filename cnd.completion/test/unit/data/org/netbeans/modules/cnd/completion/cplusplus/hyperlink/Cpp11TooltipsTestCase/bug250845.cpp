namespace bug250845 {
    int main250845() {
        const int value = 3;
        auto x = value;     // int          
        auto y = &value;    // const int *   
        const int* ptr = &value;
        auto z = ptr;       // const int *   
        auto *zz = ptr;         // const int *
        const auto *zzz = ptr;     // const int *

        const int arr[2] = {1, 2};
        for (auto elem : arr) { // int
            elem = 5;   
        }

        const int * arr2[2] = {&arr[1], &arr[2]};
        for (auto elem : arr2) { // const int *
            *elem;
        }

        int intVal1 = 0;
        int intVal2 = 1;
        int * arr3[2] = {&intVal1, &intVal1};
        for (auto &elem : arr3) { // int *&
            elem = &intVal2;
        }

        int arr4[2] = {intVal1, intVal2};
        for (const auto &elem : arr4) { // const int &
            elem;
        }
    }
}