__all__ = [ "MyPublikClass", "MyPublicClass" ]
__all__.extend(["makedirs", "removedirs", "renames"])
__all__.append("three")

class MyPublicClass:
    def mymethod(self):
        pass

class MyPrivateClass:
    def mymethod2(self):
        pass


