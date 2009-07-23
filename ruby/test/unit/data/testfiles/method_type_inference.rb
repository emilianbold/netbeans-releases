class A

  def method_as_exit_point
    simple_int
  end

  def return_self
    self
  end

  def simple_int
    42
  end

  def simple_array
    [42, 0]
  end

  def condition
    if want_fixnum
      return 42
    elsif want_string
      return "42"
    else
      return 42.0
    end
  end

  def condition2
    if want_fixnum
      42
    elsif want_string
      return "42"
    else
      42.0
    end
  end

  def condition3
    if want_fixnum
      return 42
    elsif want_string
      return "42"
    else
      return 42.0
    end
    nevim
  end

end

a = A.new
num = a.simple_int
puts num.abs
puts a.method_as_exit_point

