
class AttributeTest:
    def __init__(self):
        self.okay = true;
        self._private = true;

    def notconstructor(self):
        self.notokay = true;
        
        
        print self.okay
        print self.notokay
        

    def usage(self):
        other.notokay = true;
        other._notokay = true;
        print other._notokay
        

