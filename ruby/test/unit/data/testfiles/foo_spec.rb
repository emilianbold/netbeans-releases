describe "shared example", :shared => true do
  it "shared" do
    puts "hello"
  end
end

describe "Some spec" do

  it_should_behave_like "shared"

  it "something else" do
    puts "world"
  end
end

