# # This is a comment
# Ruby documentation (RDoc) conventions:
# :nodoc:   <= rdoc directive
# _italic_  <= underline-surrounded words get displayed as italic
# *bold*    <= asteriks-surrounded words get displayed as bold
# +code+    <= plus-surrounded words are formatted as code (monospace font)
# Class#method, #method, http://netbeans.org   <= References are displayed as links
# TODO markers are also highlighted

class Foo                   # Ruby keywords are blue, constants are italic
  include Foo               # include and required are NOT keywords in Ruby
  require 'hello'           # They are method calls - shown as bold in NetBeans

  def hello(arg1)           # Parameters are orange
    localvar = 5            # A yellow highlight marks all occurences of the symbol under the caret
    puts arg1, localvar     # So are usages of parameters
    { :symbol => "value" }  # Symbols are cyan
    /myregexp/              # Regular expression literals are purple
    %r(regexp)              # 
    "string"                # Strings are orange
    %Q(escapes like \n and \C-x are bold)
    'no newline:\n'
    @instancevar            # Instance vars are green
    @@classvar              # So are classvars - and they are italic as well
    unusedvar = 50          # Unused variables are underlined in gray
    then                    # Errors are underlined in red
    puts localvar
    # mixed code
    "#@instance variable" # :nodoc: highlighted directive
    "#@@class variable"  
    "#$global variable"  
    /text #@instance variable text/
    /text #{ hello(5) } text/
    "code in #{a = hello(5, 6) } string"
  end
  
  def hello(arg1, arg2)
  end
  
  def hello()
    hello(1)
    hello(1, 2)
  end
  
  def hello1(*args, &block)
    hello(*args, &block)
  end
end
