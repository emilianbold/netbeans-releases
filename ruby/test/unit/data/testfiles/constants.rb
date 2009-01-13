GLOBAL_CONSTANT = 'Global Constant'

module Colors

  module Converter
    VERSION = 1234
    # module definition
  end

  RED   = "#FF0000"
  GREEN = "#00FF00"
  BLUE  = "#0000FF"

  def red
    return RED
  end

end

Colors::RED.bytes
Colors::Converter::VERSION
puts GLOBAL_CONSTANT

b = Colors::BLUE
puts b.downcase