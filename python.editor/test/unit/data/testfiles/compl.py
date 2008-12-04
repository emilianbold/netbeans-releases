class SuperSuper:
    def mysupersupermethod(self):
        print "World"

class Super(SuperSuper):
    def mysupermethod(self):
        print "Hello"



class MyClass(Super):
    def mymethod(self):
        print "Hello World"
        self.mysupersupermethod()
        print dir()

x = MyClass()
x.mymethod()

