module Commode
  module Wrappers

    # Returns the bots current nick
    def nick
      @irc.me
    end

    # Changes the bots nick
    def nick=(value)
      @irc.nick(value)
    end

    # Say something
    def send(target, text)
      @irc.msg(target, text)
    end

    # Join specified channel, with optional password
    def join(channel, password = '')
      @irc.join(channel, password)
    end

    # Leaves a channel, with optional reason
    def part(channel, reason = '')
      @irc.part(channel, reason)
    end

  end
end
