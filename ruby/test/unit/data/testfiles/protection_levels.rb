# Tests to ensure that I properly record the various protection levels
class Foo
  def i_am_public
  end
  
  def i_am_actually_private
  end
  
  private
  def i_am_private
  end
  
  class << self # Class methods
    def im_a_static_public
    end

    def im_actually_static_and_private
    end
    
    private    
    def im_a_static_private
    end
  end
  
  private :i_am_actually_private, :im_actually_static_and_private
end