class Date
  def self.valid_ordinal? (y, d, sg=ITALY)
    if d < 0
      ny, = clfloor(y + 1, 1)
      jd = ordinal_to_jd(ny, d + 1, sg)
      ns = ns?(jd, sg)
      return unless [y] == jd_to_ordinal(jd, sg)[0..0]
      return unless [ny, 1] == jd_to_ordinal(jd - d, ns)
    else
      jd = ordinal_to_jd(y, d, sg)
      return unless [y, d] == jd_to_ordinal(jd, sg)
    end
    jd
  end

  private
  def split_name name
    raise TooLongFileName if name.size > 256
    if name.size <= 100
      prefix = ""
    else
      parts = name.split(/\//)
      newname = parts.pop
      nxt = ""
      loop do
        nxt = parts.pop
        break if newname.size + 1 + nxt.size > 100
        newname = nxt + "/" + newname
      end
      prefix = (parts + [nxt]).join "/"
      name = newname
      raise TooLongFileName if name.size > 100 || prefix.size > 155
    end
    return name, prefix
  end

end


