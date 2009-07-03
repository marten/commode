module Commode
  module Modules
    class Braces < AbstractModule

      OPEN_REGEXP = /\([A-Za-z0-9]/
      CLOS_REGEXP = /[A-Za-z0-9]\)/

      SMILIES_REGEXP = /[;:X]-*[})>{(<|DO]|
                        [})>{(<|O]-*[;:X]|
                        [|][:]-*[(]|
                        [:][']-*[(]/x
  

      def initialize(bot)
        super(bot)
        @braces   = Hash.new(0)
        @activity = Hash.new()
      end

      def reload
        load __FILE__
      end

      def incoming_channel(fullactor, nick, channel, text)
        # Disable this
        unless true ||  @bot.silenced?
          update_brace_count(channel, text)
          balance_braces(channel)
          @activity[channel] = Time.now
        end
      end
  
      private
  
      def strip_smilies(str)
        str.gsub(SMILIES_REGEXP, "")
      end

      def update_brace_count(channel, text)
        sanitized_text = strip_smilies(text)
        @braces[channel] += sanitized_text.count("(")
        @braces[channel] -= sanitized_text.count(")")
        @braces[channel]  = 0 if @braces[channel] < 0
      end

      def balance_braces(channel)
        if @braces[channel] > 0
          @bot.say(channel, ")"*@braces[channel])
        end
        @braces[channel] = 0
      end

    end
  end
end