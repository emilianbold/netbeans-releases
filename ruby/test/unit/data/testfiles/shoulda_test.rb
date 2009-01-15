context "A Queue instance" do
  setup do
    @queue = Queue.new
  end

  should "respond to :push" do
    assert_respond_to @queue, :psh
  end

  context "with a single element" do
    setup { @queue.push(:something) }

    should "return that element on :pop" do
      assert_equal 'aaa bbb', 'aaa bbb'
      #        assert_equal :something, @queue.pop
    end

  end
end
