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

Colors::RED.byte
Colors::Converter::VERSION
