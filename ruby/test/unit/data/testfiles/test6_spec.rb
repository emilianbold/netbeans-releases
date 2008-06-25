# From http://blog.davidchelimsky.net/articles/tag/rspec
describe ThingsController, "GET #index" do
  fixtures :things

  act! { get :index }

  before do
    @things = []
    Thing.stub!(:find).with(:all).and_return(@things)
  end

  it_assigns :things
  it_renders :template, :index
end

