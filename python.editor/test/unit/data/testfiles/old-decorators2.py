def foo(cls):
    pass
foo = synchronized(lock)(foo)
foo = classmethod(foo)

