# see IZ 115928
class First
  attr_accessor :title

  def initialize(init)
    @title = init
  end

  def mine
    puts self.title
  end
end