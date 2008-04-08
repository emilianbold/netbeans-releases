class AddNames < ActiveRecord::Migration
  def self.up
    rename_column(:posts, "address", "newaddress")
  end

  def self.down
  end
end
