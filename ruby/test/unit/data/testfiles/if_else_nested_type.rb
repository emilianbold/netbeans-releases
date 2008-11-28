var = nil
if cond1
  var = 1.0
  if cond2
    var = "somestring"
    var = {:a => 1, :b => 2}
    var.ifcond2
  else
    var = [1, 2, 3]
    var.elsecond2
  end
  var.ifcond1a
  var = 1
  var.ifcond1b
end
var.def
