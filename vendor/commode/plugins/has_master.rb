module Commode
  module Plugins
    module HasMaster

      def is_master?(actor)
	actor == @master
      end

      def when_is_master(actor)
	yield if is_master(actor)
      end

    end
  end
end
