class FooController < ApplicationController

  def bar
   puts @flash 
  end

  def baz
    puts @request
  end
end
