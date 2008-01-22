require 'rbconfig'

# Used by NetBeans to gather information about a Ruby Platform. Output is
# properties file format so NetBeans can easily parse it through
# java.util.Properties

# Interpreter info
jruby = defined?(JRUBY_VERSION)
$stdout.printf "ruby_kind=#{jruby ? "JRuby" : "Ruby"}\n"
$stdout.printf "ruby_version=#{RUBY_VERSION}\n"
$stdout.printf "jruby_version=#{JRUBY_VERSION}\n" if jruby
$stdout.printf "ruby_patchlevel=#{RUBY_PATCHLEVEL}\n" if defined? RUBY_PATCHLEVEL
$stdout.printf "ruby_release_date=#{RUBY_RELEASE_DATE}\n"
RbConfig = Config unless defined?(RbConfig) # 1.8.4 support
ruby = File.join(RbConfig::CONFIG["bindir"], RbConfig::CONFIG["ruby_install_name"])
ruby << RbConfig::CONFIG["EXEEXT"]
$stdout.printf "ruby_executable=#{ruby}\n"
$stdout.printf "ruby_platform=#{RUBY_PLATFORM}\n"

# RubyGems info
begin
  require 'rubygems'
  $stdout.printf "gem_home=#{Gem.dir}\n" 
  $stdout.printf "gem_path=#{Gem.path.join(":")}\n" 
  $stdout.printf "gem_version=#{Gem::RubyGemsVersion} (#{Gem::RubyGemsPackageVersion})\n" 
  $stdout.printf "\n" 
rescue LoadError
  # no RubyGems installed
end
