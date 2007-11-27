class FooBar
  GOOD_CONSTANT = 50
  BadConstant = 50
   
  # My method
  # Has documentation
  private
  def good_method
    good_symbol = 50
    x = 50
    callmethod(callmethod2(50+30))
    callmethod3(callmethod4(x+30))
    puts good_symbol
    a = 25
    b = "string"
    c = /regexp/
    d = a+b-c
  end

  # Another method
  def badMethod
    badSymbol = 50
    puts badSymbol
  end
   
end

