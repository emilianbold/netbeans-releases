class A:
    def foo():
        self.filename = filename

def toplevel(x,y,z):
    print "hello"
    
def toplevel2():
    print "hello"
    
class Bar:
    def noargs():
        print "hello"
        
    def okay1(self):
        print "hello"

    def okay2(cls):
        print "hello"

    def okay3(self, bar):
        print "hello"

    def okay4(cls, bar):
        print "hello"
        
    def bad1():
        print "hello"

    def bad2(filename):
        print "hello"
