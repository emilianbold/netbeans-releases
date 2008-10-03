# Duplicated from activerecord because during unit tests, we don't seem
# to find this in the index
class ActiveRecord::ConnectionAdapters::SchemaStatements
    def create_table(table_name, options = {})
    end
end

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
