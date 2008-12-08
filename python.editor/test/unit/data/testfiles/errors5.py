
__author__="tor"
__date__ ="$Oct 16, 2008 4:19:21 PM$"

if __name__ == "__main__":
    print "Hello";

class C
def foo():
    doc = "The foo property."
    def fget(self):
        return self._foo
    def fset(self, value):
        self._foo = value
    def fdel(self):
        del self._foo
    return locals()
    foo = property(**foo())

print foo
