  specify "should be valid with a full set of valid attributes" do
    @user.attributes = valid_user_attributes
    @user.should_be_valid
  end

