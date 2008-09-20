class UsersController < ApplicationController
  def index
    @enabled, @disabled = @users.partition { |u| u.deleted_at.nil? }
  end
  
  def show
    @user  = User.new
  end

end
