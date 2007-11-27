# From date.rb
class Date
  def self.civil_to_jd(y, m, d, sg=GREGORIAN)
    if m <= 2
      y -= 1
      m += 12
    end
    a = (y / 100.0).floor
    b = 2 - a + (a / 4.0).floor
    jd = (365.25 * (y + 4716)).floor +
      (30.6001 * (m + 1)).floor +
      d + b - 1524
    if os?(jd, sg)
      jd -= b
    end
    jd
  end
end


