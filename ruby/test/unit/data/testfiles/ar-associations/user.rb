class User < ActiveRecord::Base
  has_many :projects
  has_many :details, :class_name => "UserDetail", :foreign_key => 'some_id'
  has_many_polymorphs :dependents,  :from => [:projects, :user_details]
end