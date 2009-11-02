module Foo
  module Bar
    class Baz
      def qux
        77
      end
    end
  end
end

seventy_seven = Foo::Bar::Baz.new.qux