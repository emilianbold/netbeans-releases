ancestors = String.ancestors
ancestors.delete(String)
puts "Ancestors of String class:\n  * #{ancestors.join("\n  * ")}"

# simple non-chaining multiple type inference
has_one = [1,2].include?(1)
puts has_one.t

# simple non-chaining multiple type inference of non-Module method
a = -123.abs
huh = a.eql?(123)
