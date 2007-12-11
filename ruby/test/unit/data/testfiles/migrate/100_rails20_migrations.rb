class Rails20Migrations < ActiveRecord::Migration
  def self.up
    create_table :people do |t|
      t.integer :account_id
      t.string  :first_name, :last_name, :null => false
      t.text    :description
      t.timestamps
    end    
  end

  def self.down
  end
end

