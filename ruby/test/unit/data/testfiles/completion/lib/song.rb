# From http://www.netbeans.org/issues/show_bug.cgi?id=98852
#super song
class Song
  @@songcounter=0
  #new comment
  def initialize(name="test")
    @@songcounter=@@songcounter+1
    @name=name
  end
  attr_accessor :age, :name
  def sing(text="lala")
    puts text
  end
end

ss = Song.new()
ss.sing # is completed
ss.age # not completed
ss.songcounter # is not shown, but
#ss.@@songcounter # is shown

