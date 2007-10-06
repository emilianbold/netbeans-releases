
class CreateProducts < ActiveRecord::Migration
  LHS = 50
  def self.up
    create_table(firstarg,  :id => true)
    create_table firstarg,  :id => true
    create_table :products do |t|
      t.column :title,       :string
      t.column :description, :text
      t.column :image_url,   :string
    end
    add_column(f, column_name, type, options)
  end

  def self.down
    drop_table :products
  end
end
