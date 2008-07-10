# From http://www.lukeredpath.co.uk/2006/8/29/developing-a-rails-model-using-bdd-and-rspec-part-1
module UserSpecHelper
  def valid_user_attributes
    { :email => 'joe@bloggs.com',
      :username => 'joebloggs',
      :password => 'abcdefg' }
  end
end

context "A user (in general)" do
  include UserSpecHelper

  setup do
    @user = User.new
  end

  specify "should be invalid without a username" do
    @user.attributes = valid_user_attributes.except(:username)
    @user.should_not_be_valid
    @user.errors.on(:username).should_equal "is required" 
    @user.username = 'someusername'
    @user.should_be_valid
  end

  specify "should be invalid without an email" do
    @user.attributes = valid_user_attributes.except(:email)
    @user.should_not_be_valid
    @user.errors.on(:email).should_equal "is required" 
    @user.email = 'joe@bloggs.com'
    @user.should_be_valid
  end

  specify "should be invalid without a password" do
    @user.attributes = valid_user_attributes.except(:password)
    @user.should_not_be_valid
    @user.password = 'abcdefg'
    @user.should_be_valid
  end
end


