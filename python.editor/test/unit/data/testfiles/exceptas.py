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

