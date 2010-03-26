class Dummy

  def foo
    if cond1
      return "somestring"
    end
    [1, 2, 3]
  end

  def boo(unusedparam, unusedparam2, usedparam)
    unusedparam2 = 5 # Written but not read - still unused!
    unusedlocal1 = "foo"
    usedlocal2 = "hello"
    usedlocal3 = "world"
    puts usedparam
    x = []
    x.each { |unusedblockvar1, usedblockvar2|
      puts usedblockvar2
      puts usedlocal2
    }
    puts usedlocal3
  end

  def baz
    if cond
      "99"
    else
      99
    end
  end

  def bar
    if cond
      then
      "if"
    elsif cond2
      "elsif"
    else
      "else"
    end
  end

  def qux
    case switch
    when 1 then "1"
    when 2 then "2"
    else 2
    end
  end

  def thud
    if true
      call "1"
      "2"
    end
  end

  def corge(z)
    z ? "a" : nil
  end

  def quux
    true || false || thud || random.call
  end

  def fred
    some.call
  end

  def barry(p)
    raise "huh" if p
    "77"
  rescue => ex
    77
  end
end
