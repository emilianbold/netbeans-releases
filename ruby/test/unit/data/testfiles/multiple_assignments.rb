class MultAssgn
  def initialize
    @a,@b = 1,"b"
    @c, (@d, (@e, @f)) = "c", [3, ["e", "f"]]
  end
end