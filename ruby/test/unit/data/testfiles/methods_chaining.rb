greeting = "hello world \n".chomp.chop
puts greeting.capitalize

# try on variable
puts greeting.empty?.to_s

# try on literal
puts 1.even?.to_s

# try chaining without parenthesis
puts greeting.capitalize.swapcase

# try chaining with parenthesis without params
puts greeting.capitalize().swapcase()

# try parenthesised calls with parameters expression
10.between?(0, 100).to
