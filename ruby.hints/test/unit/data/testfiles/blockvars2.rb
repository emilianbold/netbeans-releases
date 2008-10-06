class Foo
  def bar(local)
    local = 50
    fo(50) { |x| puts x }
    3.14.each { |local|
        puts local  
    }
    puts local
     puts @request
    
  end

  def forloop
    i = 50
    for i in 0 .. 0x3f
      SUCC['s'][i.chr] = 0x40 - i
    end
    puts i
  end
end
