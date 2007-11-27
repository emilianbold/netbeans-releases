  def bla
    ["a", "b", "c"].each {|x| sleep 0.5; yield x}
  end

