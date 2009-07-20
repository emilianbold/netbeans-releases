class Bar
  attr_accessor :baz
  def qux
    @baz = 1
    @thud = ""
  end
end

class Corge < Bar
  attr_accessor :thud
end

b = Bar.new
b.baz

c = Corge.new
c.thud