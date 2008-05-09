require "not"
require 'optparse'
require 'cgi-lib'
require 'importenv'
require 'parsearg'
require "getopts"
require_gem "rails"
require "ftools"
require "fileutils"

def foo
  assert_raises(AssertionFailedError){ assert_select "p" }
end


