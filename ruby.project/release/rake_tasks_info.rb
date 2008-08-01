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
  # check whether new methods from Rake 0.8.x are available
  comment = t.respond_to?(:full_comment) ?
    (t.full_comment ? t.full_comment.gsub(/\n/, '\n') : '') :
    t.comment
  name = t.respond_to?(:name_with_args) ? t.name_with_args : t.name
  puts "#{name.gsub(/:/, '\:')}=#{comment}"
end

