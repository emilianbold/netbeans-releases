class Rails20Migrations < ActiveRecord::Migration
  def self.up
    create_table :people do |t|
      t.integer :account_id
      t.string  :first_name, :last_name, :null => false
      t.text    :description
      t.timestamps
    end    
    
    #    create_table "products", :force => true do |t| 
    #      t.column "shop_id",    :integer 
    #      t.column "creator_id", :integer 
    #      t.column "name",       :string,   :default => "Untitled" 
    #      t.column "value",      :string,   :default => "Untitled" 
    #      t.column "created_at", :datetime 
    #      t.column "updated_at", :datetime 
    #    end

    create_table :products do |t| 
      t.integer :shop_id, :creator_id 
      t.string  :name, :value, :default => "Untitled" 
      t.bogus  :name, :value, :default => "Untitled" 
      t.zzz  :name, :value, :default => "Untitled" 
      t.aaa  :name, :value, :default => "Untitled" 
      t.date  :mydate :default => "Untitled" 
      t.timestamps 
    end 
  end

  def self.down
  end
end

