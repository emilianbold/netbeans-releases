class Ancestor:
    def overridden_method1(self, a, b):
        pass
    def overridden_method2(self, c, d):
        pass

class Middle(Ancestor):
    def overridden_method1(self, a, b):
        pass

class Middle2(Ancestor):
    def overridden_method2(self, a, b):
        pass

    def overridden_method1(self, a, b):
        pass

class Child(Middle, Middle2):
    def overridden_method1(self, a, b): # Final
        pass
    def overridden_method2(self, c, d): # Final
        pass

