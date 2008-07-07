# Used by NetBeans to gather information about Rake Tasks for a Ruby project.
# Output is properties file format so NetBeans can easily parse it through
# java.util.Properties

require 'rubygems'
require 'rake'

app = Rake.application
app.do_option('--silent', nil)
app.init
app.load_rakefile
commented_tasks = app.tasks
commented_tasks.each do |t|
  comment = (t.full_comment ? t.full_comment.gsub(/\n/, '\n') : '')
  puts "#{t.name_with_args.gsub(/:/, '\:')}=#{comment}"
end

