# From 122709
class MatchHashOrAttribute < Match
  def initialize arg
    @matches = {}
    arg.each_pair do |foo, val|
      @matches[foo] = Match.match_factory(val)
    end
  end
    
  def match? rhs
    result = false
    @matches.each_pair do |foo, val|
      if Hash === rhs
        matched = val.match?(rhs[foo])
      else
        (matched = val.match?(rhs.send(foo))) rescue nil
      end
      if matched
        result = true
      else
        result = false
        break
      end
    end
    result
  end
end


