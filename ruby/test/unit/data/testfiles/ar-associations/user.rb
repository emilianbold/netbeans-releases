class User < ActiveRecord::Base
  has_many :projects
  has_many :details, :class_name => "UserDetail", :foreign_key => 'some_id'
end