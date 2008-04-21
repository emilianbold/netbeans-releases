class CreatePosts < ActiveRecord::Migration
  def self.up
    create_table :posts do |t|
      t.column :title, :text
      t.column :subtitle, :string
      t.column :removedlater, :string
      t.column "importance", :integer
      t.timestamps
    end
  end

  def self.down
    drop_table :posts
  end
end
