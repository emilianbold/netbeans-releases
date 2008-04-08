# unless to if conversion is broken because
# the not-node here has wrong offsets (points to !(x < 5 without the closing )
unless !(x < 5)
  puts "Hello"
end


