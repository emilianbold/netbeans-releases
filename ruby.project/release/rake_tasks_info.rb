# Used by NetBeans to gather information about Rake Tasks for a Ruby project.
# Output is properties file format so NetBeans can easily parse it through
# java.util.Properties

require 'rubygems'
require 'rake'

app = Rake.application
app.do_option('--silent', nil)
app.init
app.load_rakefile
commented_tasks = app.tasks.select { |t| t.comment }
commented_tasks.each do |t|
  puts "#{t.name_with_args.gsub(/:/, '\:')}=#{t.full_comment}"
end

