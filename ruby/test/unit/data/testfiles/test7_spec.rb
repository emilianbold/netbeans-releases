# From http://blog.davidchelimsky.net/articles/tag/rspec
context "on GET to :show for first record" do
  setup do
    get :show, :id => 1
  end

  should_assign_to :user
  should_respond_with :success
  should_render_template :show
  should_not_set_the_flash

  should "do something else really cool" do
    assert_equal 1, assigns(:user).id
  end
end

