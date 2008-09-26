
class MyTest
  @myfield = 50
  @myotherfield = 50
  @another = 50
  x = "Hello World"
  y = /reg/
  z = 3.14
  d = true
  puts "Result is #{@myfield} and #@another."
  puts Module.class_variables
  puts 'Hello'.class
end
