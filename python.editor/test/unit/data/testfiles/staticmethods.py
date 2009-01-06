class Page(object):
  def __init__(self, name, entity=None):
    self.name = name
    self.entity = entity

  @staticmethod
  def load(name):
    pass

  @staticmethod
  def exists(name):
    pass


