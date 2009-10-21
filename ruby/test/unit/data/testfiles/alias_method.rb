class Far
  def far_away

  end

  alias_method :far_far_away, :far_away
end

Far.new.far_far_away