# __NAME__.rb
# __DATE__
#

$:.unshift File.join(File.dirname(__FILE__),'..','lib')

require 'test/unit'

class Test__CAPITALIZEDIDENTIFIER__ < Test::Unit::TestCase
#  def setup
#  end
#
#  def teardown
#  end

  def test_fail
    assert(false, 'Assertion was false.')
  end
   
  def test_foo
    # assert_equal("foo", bar)

    # assert, assert_block, assert_equal, assert_in_delta, assert_instance_of,
    # assert_kind_of, assert_match, assert_nil, assert_no_match, assert_not_equal,
    # assert_not_nil, assert_not_same, assert_nothing_raised, assert_nothing_thrown,
    # assert_operator, assert_raise, assert_raises, assert_respond_to, assert_same,
    # assert_send, assert_throws

    flunk "TODO: Write test"
  end
end
