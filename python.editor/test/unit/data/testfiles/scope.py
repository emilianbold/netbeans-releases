import module1
import module2
import module3 as module4

toplevelvar = 1
toplevelvar2 = 2
toplevelvar3 = 3
toplevelvar4 = 4

def myfunc(funcparam):
    localvar = 1
    toplevelvar4 = 6
    print toplevelvar4
    pass

class MyClass(SuperClass):
    var_in_class = 1
    def mymethod(self,param1,param2):
        in_method = 1
        in_method = 2
        print in_method
        print var_in_class
        print toplevelvar
        toplevelvar3 = 3
        def myfunc():
            in_func = 1
            print in_method
            print in_func
            in_method = 2

    def othermethod(self,param3):
        pass

