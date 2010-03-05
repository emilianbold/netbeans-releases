x = 1
y = 2
case (x)
when 1:
    puts "hello"
end
class GuessName
  def call1(*foo)
  end
  def call2(&foo)
  end
  def call3(a,b=2)
  end
  def call4(a,b,c,&d)
  end
  def do_test
    call1(x)
    call2(y)
    call3(x,y,z)
    call4(x,y,z,w)

    call( (x<y) && true, 2)
    if (x < y)
      puts "yes"
    else
      puts "no"
    end

    puts x,
      y+1

    puts x,
      (1+y)
  end
end

