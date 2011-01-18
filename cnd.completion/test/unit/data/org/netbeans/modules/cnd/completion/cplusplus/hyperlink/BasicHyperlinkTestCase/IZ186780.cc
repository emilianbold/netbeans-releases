
struct str2int186780 {
    char *string;
    int value;
};

struct str2int186780 c_list186780[] =
{
    {"T_REG", 1},
    {"T_ASCII", 2},
    {0, 0},
};

static int conv186780(char *str)
{
    for (int counter = 0; c_list186780[counter].string; ++counter) {
        return c_list186780[counter].value;
    }
    return 0;
}
