class TestFoo < Test::Unit::TestCase
 def test_bar
 end
end

module MosModule
 class TestBaz < Test::Unit::TestCase
   def test_qux
   end
   def test_two
   end
 end
end


module MosModule
 class TestBaz < Test::Unit::TestCase
   def test_two # Has two definition sites
   end
 end
end


