class Far
  def far_away

  end

  alias_method :far_far_away, :far_away
end

Far.new.far_far_away

class Close
  def so_close

  end
  # for testing dsymbolnodes
  alias_method :"so_so_close", :"so_close"
end

Close.new.so_so_close

class Here
  def right_here
  end
  # 'alias' keyword
  alias :right_there :right_here

end

Here.new.right_there