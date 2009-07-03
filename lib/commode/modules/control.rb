module Commode
  module Modules
    class Control < AbstractModule

      include Commode::Plugins::HasMaster

      attr_accessor :master

      def initialize(bot)
        super(bot)
        @master = "marten"
      end

      def incoming_channel(fullactor, actor, target, text)
        case text

        when /^#{@bot.nick}[:,] verander je naam in (.*)/
          return unless actor == @master
          @bot.nick = $1
          return false

        when /^#{@bot.nick}[:,] scheer je weg!/
          return unless actor == @master
          @bot.part(target)
          return false
      
        when /^#{@bot.nick}[:,] kom je ook naar (\#.*)\??/
          return unless actor == @master
          @bot.join($1)
          @bot.say(target, "Zie je daar!")
          return false
      
        when /^#{@bot.nick}[:,]\s(stil\seens|
                                  stil\sjij|
                                  ga\sdood|
                                  sterf|
    			      stfu|
                                  donder\sop|
                                  is\sje\snumwis2\sal\saf\??)/x
          @bot.silence(300)
          @bot.say(target, "Ok. Aan de numwis2 maar weer!")
          return false
    
        when /^#{@bot.nick}[:,] herlaad/
          return unless actor == @master
          @bot.reload
          @bot.say(target, "Ok, #{actor}. Helemaal nieuw en glimmend.")
          return false
    
        end
      end

      def incoming_invite(fullactor, actor, target)
        return unless actor == @master
        @bot.join(target)
      end

      def incoming_join(fullactor, actor, target)
        return unless actor == @master
        @bot.op(target, @master)
      end

      def reload
        load __FILE__
      end

    end
  end
end