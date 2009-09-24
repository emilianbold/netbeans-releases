# doesn't extend ActionController::Base, so this isn't really a controller
# - so no rails deprecation hints should be displayed for this
class NotAController

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

  private
  def i_am_private
  end

  protected
  def i_am_protected
  end

#  public
#  def i_am_public
#  end

end
