class Apple
  def foo
    snark %w(a b c)

  end
end

def foo
  snark %w(a b c)
  snark %W(a b c)
  snark %q(a b c)
  snark %Q(a b c)
  snark %w(a b c)
  snark %x(a b c)
  snark %r(a b c)
  snark %s(a b c)

  a = %w(a b c)
  a = %W(a b c)
  a = %q(a b c)
  a = %Q(a b c)
  a = %w(a b c)
  a = %x(a b c)
  a = %r(a b c)
  a = %s(a b c)

  snark(%w(a b c))
  snark(%W(a b c))
  snark(%q(a b c))
  snark(%Q(a b c))
  snark(%w(a b c))
  snark(%x(a b c))
  snark(%r(a b c))
  snark(%s(a b c))

end
