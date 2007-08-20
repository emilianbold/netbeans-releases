class ActionView::Base
_buf=''; define 'Root' do |idx, doc| 
_buf << '	'; _buf << ( idx).to_s;;_buf << ' '; _buf << ( title ).to_s;;_buf << ' in '; _buf << ( doc.title ).to_s;
_buf << '';  end 
_buf << '

'; 
end
