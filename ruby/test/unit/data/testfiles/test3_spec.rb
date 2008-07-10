# From http://www.lukeredpath.co.uk/2006/8/29/developing-a-rails-model-using-bdd-and-rspec-part-1
context "A user (in general)" do
  setup do
    @user = User.new
  end

  specify "should be invalid without a username" do
    @user.email = 'joe@bloggs.com'
    @user.password = 'abcdefg'
    @user.should_not_be_valid
    @user.errors.on(:username).should_equal "is required" 
    @user.username = 'someusername'
    @user.should_be_valid
  end

  specify "should be invalid without an email" do
    @user.username = 'joebloggs'
    @user.password = 'abcdefg'
    @user.should_not_be_valid
    @user.errors.on(:email).should_equal "is required" 
    @user.email = 'joe@bloggs.com'
    @user.should_be_valid
  end

  specify "should be invalid without a password" do
    @user.email = 'joe@bloggs.com'
    @user.username = 'joebloggs'
    @user.should_not_be_valid
    @user.password = 'abcdefg'
    @user.should_be_valid
  end
end

