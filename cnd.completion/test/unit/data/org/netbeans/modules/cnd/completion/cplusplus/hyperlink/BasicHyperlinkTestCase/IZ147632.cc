namespace IZ147632 {

    int i;

    int main() {
        int i = 1;
        if (i == 1)
            ::IZ147632::i = 2;
        while (i == 2)
            ::IZ147632::i = 3;
        for (; 0;)
            ::IZ147632::i = 4;
        return 0;
    }

}
