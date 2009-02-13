class QueueTest < Test::Unit::TestCase

  should "be empty" do
    assert @queue.empty?
  end


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
      end

    end

    context "with a space at the end " do
      should "work  " do
        assert true
      end
    end

  end

end