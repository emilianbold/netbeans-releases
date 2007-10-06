class User < ActiveRecord::Base
end

@product = Product.find(params[:id])

class CreateProducts < ActiveRecord::Migration
  def self.up
    create_table :products do |t|
      t.column :title,       :string
      t.column :description, :text
# caret
      t.column :image_url,   :string
    end
  end
end

