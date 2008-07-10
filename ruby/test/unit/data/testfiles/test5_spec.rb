# From http://blog.davidchelimsky.net/articles/tag/rspec
describe ThingsController do
  context "handling GET /things/1" do
    before(:each) { get :show, :id => "1" }
    should_respond_with :success
  end
end

