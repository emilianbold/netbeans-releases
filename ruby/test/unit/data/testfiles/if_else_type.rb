def m
  var = nil
  if cond1
    var = "somestring"
  else
    var = [1, 2, 3]
  end
  p var.in
end
