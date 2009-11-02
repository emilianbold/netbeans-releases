void debug(const char *file, int line, const char *msg, ...)
    __attribute__((format(printf, 3, 4)));

#ifndef NO_DEBUG
  #define debug(msg, args...) debug(__FILE__, __LINE__, msg, ##args)
#else
  #define debug(msg, args...)
#endif

int main(int argc, char** argv)
{
  debug("eee"); // UNABLE TO RESOLVE IDENTIFIER debug
  return 0;
}