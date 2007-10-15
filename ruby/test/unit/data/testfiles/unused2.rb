class Foo1
  def mymethod arg1, arg2, arg3
    puts arg1, arg2, arg3
  end
end

class Foo2 < Foo1
  # Can't mark args thought to be unused as unused
  # if we have a super call without args (which will
  # actually pass them)
  def mymethod args1, args2, args3 = {}
    super
    puts "Hello"
    puts args2
  end
end

class Foo3 < Foo1
  def mymethod args1, args2, args3 = {}
    super 1, 2, 3
    puts "Hello"
    puts args2
  end
end

Foo2.new.mymethod("TestArg1", "TestArg2", "TestArg3")

