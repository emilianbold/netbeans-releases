describe Some do
  before(:all) do
    @some = Some.new
    @thing = Thing.new
  end

  it "should do stuff" do
    @some.stuff
  end

  it "should do it" do
    @thing.do_it
  end

end