def m
  var = nil
  if cond1
    var = "somestring"
  elsif cond2
    var = {:a => 1, :b => 2}
  else
    var = [1, 2, 3]
  end
  p var.in
end
