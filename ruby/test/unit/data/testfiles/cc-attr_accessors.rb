class FooBarBaz
  attr_reader :bar
  attr_writer :baz, :foo
  def initialize
    @foo,@bar,@baz = 1,2,3
  end
end