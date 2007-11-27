class Foo
 attr_accessor :bar
 attr_reader :onlyread
 attr_writer :boo

 def initialize(value)
   bar = value
   baz = value
   boo = value
   onlyread = value # read-only attrs don't count
 end
  def arg(bar=50) # Not a reference
  end
end

# Should NOT be referencing attrs elsewhere
class OtherClass
  def whatever
    bar = 50
  end
end

puts Foo.new(42).bar

