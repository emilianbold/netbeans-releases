int plus(int x, int y)  { return x + y; }
int minus(int x, int y) { return x - y; }

int (*get_ptr(const char opCode))(int, int) {
	if(opCode == '+')
		return &plus;
	else
		return &minus;
}
