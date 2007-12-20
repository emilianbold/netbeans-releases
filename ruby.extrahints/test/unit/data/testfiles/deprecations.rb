require "not"
require 'optparse'
require 'cgi-lib'
require 'importenv'
require 'parsearg'
require "getopts"
require_gem "rails"

def foo
  assert_raises(AssertionFailedError){ assert_select "p" }
end


