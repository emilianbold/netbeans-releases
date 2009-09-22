class MyController < ApplicationController
  def update
    # This line should produce a warning
    @product = Product.find_all(params[:id])
    # ... and so should this
    @product = Product.find_first(params[:id])
    # ... and so should this
    @product = Foo::Product.find_first(params[:id])
    # ... and so should this
    @product = Foo::Bar::Product.find_first(params[:id])
    x = []
    # but this should NOT
    x.find_all { |foo| puts foo }
    # and neither should this:
    x.find_first { |foo| puts foo }
  end
end

