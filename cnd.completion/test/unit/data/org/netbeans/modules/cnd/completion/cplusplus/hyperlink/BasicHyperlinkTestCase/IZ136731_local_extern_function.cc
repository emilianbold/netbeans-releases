
static int isptsfd(char * fd) {
    extern int _private_stat64(char * cc);
    return _private_stat64(fd);
}
