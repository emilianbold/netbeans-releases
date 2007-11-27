class CreatePosts < ActiveRecord::Migration
  def self.up
    create_table :posts do |t|
      t.column "hello", :string
    end
  end
end


