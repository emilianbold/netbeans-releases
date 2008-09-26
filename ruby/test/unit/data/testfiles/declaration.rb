require 'openssl'
require 'ftools'

class Foo
  attr_accessor :symbol
end
File.move("a","b")
File.safe_unlink("a","b")

