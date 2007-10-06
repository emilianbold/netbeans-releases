
class CreateProducts < ActiveRecord::Migration
  LHS = 50
  def self.up
    create_table(foo, :key => :a)
  end
end
