def test
  alreadyexists1 = 40
  if true
     alreadyexists2 = 50
     alreadyexists3 = 50
  end
  notusedinexpression = 60
  alreadyexists4
  # Start of expression
  newvar = 50
  puts alreadyexists1
  puts alreadyexists2
  alreadyexists4 = 50 # Written before a read - no need to pass in
  puts alreadyexists4
  alreadyexists2 = alreadyexists1 + 10
  alreadyexists3 = alreadyexists1 + 10
  notusedoutsideblock = 50
  puts notusedoutsideblock
  notusedanywhere = 30
  usedlater = 30
  # end of refactored expression
  if (true) 
    puts usedlater
    puts alreadyexists2
  end
end

