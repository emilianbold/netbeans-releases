class ActionView::Base
_buf=''; if true 
_buf << '';
 end 
_buf << '<p>
  Text before ERb.
  ';
 some_code 
_buf << '  Text after ERb
</p>

';

end
