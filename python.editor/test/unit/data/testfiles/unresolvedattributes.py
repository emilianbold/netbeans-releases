class TestUnresolved :
  def __init__(self) :
    self.defined = "defined"

  def useAttr(self):
    print self.defined   # OK
    print self.undefined # KO