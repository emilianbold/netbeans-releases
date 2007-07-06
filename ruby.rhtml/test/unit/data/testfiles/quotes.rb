class ActionView::Base
_buf=''; form_tag :action => 'update', :id => @post do 
_buf << '  '; _buf << ( render :partial => 'form' ).to_s;
_buf << '  '; _buf << ( submit_tag 'Edit' ).to_s;
_buf << '';  end 
_buf << '

'; 
end
