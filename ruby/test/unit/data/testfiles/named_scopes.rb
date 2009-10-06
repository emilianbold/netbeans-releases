class MyModel < ActiveRecord::Base
  named_scope :eka, :conditions => "blah"
  named_scope :toka, lambda {|i| { :conditions => []}}
  named_scope :kolmas, :conditions => {:ehto => true}
end