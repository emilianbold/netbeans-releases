
class _MyLocal():
    def __init__(self):
        pass

    def foo(self, param3, default=5):
        pass

    def bar(self):
        pass


def functionfoo(param1,param2):
    """This is my documentation"""
    pass


functionfoo(foo, bar)
functionfoo("foo", "bar", invalid)
y = _MyLocal()
y.foo(xyz)
y.foo(xyz, baz)

