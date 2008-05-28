# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  attr_accessor :x 
  helper :all # include all helpers, all the time

  # See ActionController::RequestForgeryProtection for details Uncomment the
  # :secret if you're not using the cookie session store
  protect_from_forgery # :secret => '888a53e2cc4bda94f67442473d91a7e7'
  x = ""
  [1,2,3].each { |foo|
    print foo
  }
  
end

