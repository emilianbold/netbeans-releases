module Included
  def self.included(clz)
    clz.extend(ClassMethods)
  end
  module ClassMethods
    def added_class_method(param)
    end
  end
end

class Includes
  include Included
  added_class_method :something
end

Includes.added_class_method

i = Includes.new
# this is an invalid call
i.added_class_method