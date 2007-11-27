class FooController < ApplicationController

  def arjs
  end

  def bar
   puts @flash 
  end

  def baz
    puts @request
  end

  def boo
  end

  def notknown
  end

  def notaction(foo)
  end

  def neednoview
    redirect_to :whatever => "whatever"
  end

end
