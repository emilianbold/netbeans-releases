ancestors = String.ancestors
ancestors.delete(String)
puts "Ancestors of String class:\n  * #{ancestors.join("\n  * ")}"

