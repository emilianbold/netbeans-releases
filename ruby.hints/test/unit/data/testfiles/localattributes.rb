class Foo
 attr_accessor :bar
  def initialize(value)
   bar = value
   baz = value
 end
end

puts Foo.new(42).bar

