module Colors
  RED   = "#FF0000"
  GREEN = "#00FF00"
  BLUE  = "#0000FF"
end

module Shapes
  CIRCLE = "circle"
end

module Outer
  OUTER_CONST = 1
  module Inner
    INNER_CONST = 2
  end
end

puts Shapes::CIRCLE
puts Colors::BLUE
puts Outer::OUTER_CONST
puts Outer::Inner::INNER_CONST
