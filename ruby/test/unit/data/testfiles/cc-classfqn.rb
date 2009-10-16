module Eka
  def self.eka_static
  end
  def eka_inst
  end
  module Toka
    def self.toka_static
    end
    def toka_inst
    end
    class Vika
      def self.vika_static
      end
      def vika_inst
      end
    end
  end
end

Eka::eka_static
Eka::Toka::toka_static
Eka::Toka::Vika::vika_static
