require 'openssl'
require 'ftools'

class Foo
  attr_accessor :symbol
end
File.move("a","b")
File.safe_unlink("a","b")

class A
  def self.file?(file_name)
  end
end

p File.file?('/tmp/huh') # be sure to not go into the A.file?
