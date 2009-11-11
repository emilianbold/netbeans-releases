class Bar
  attr_accessor :baz
  def qux
    @baz = 1
    @thud = ""
  end
end

class Corge < Bar
  attr_accessor :thud
  def fred
    @thud = ""
  end
end

b = Bar.new
b.baz

c = Corge.new
c.thud