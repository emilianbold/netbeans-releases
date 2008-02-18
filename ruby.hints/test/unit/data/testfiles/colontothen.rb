class Foo
  def foo(x)
    case x 
    when Regexp  : puts 'a regex'
    when Hash    : puts 'a regex'
    when Hash    then puts 'a regex'
    when Numeric : puts 'a number'
    when String: puts 'a string'
    end
  end
end

