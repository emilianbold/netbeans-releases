import sys
print "Hello from " + sys.version
def foo():
    pass
def bar():
    pass

try:
    foo()
except Foo as x:
    bar()
finally:
    pass

class MyError(Exception):
    def extra(self):
        pass

def test1():
    try:
        pass
    except EOFError:
        print "No var"
    except MyError, ex:
        print "Got error with %" % ex.e

def test2():
    try:
        pass
    except MyError as ex2:
        print "Got error with %" % ex2.e

