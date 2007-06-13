# line comment

foreach name ($argv)
  if ( -f $name ) then
     echo -n "delete the file '${name}' (y/n/q)?"
  endif
end
