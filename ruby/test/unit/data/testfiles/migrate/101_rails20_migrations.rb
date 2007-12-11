class Rails20Migrations < ActiveRecord::Migration
  def self.up
    create_table :products do |t| 
      t.integer :shop_id, :creator_id 
      t.string  :name, :value, :default => "Untitled" 
      t.timestamps 
    end 
  end

  def self.down
  end
end

