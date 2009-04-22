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
    
    def act(target, text)
      @irc.act(target, text)
    end
    
    def op(channel, nick)
      @irc.mode(channel, "+o", nick)
    end
    
    def deop(channel, nick)
      @irc.mode(channel, "-o", nick)
    end

    # Join specified channel, with optional password
    def join(channel, password = '')
      @irc.join(channel, password)
    end

    # Leaves a channel, with optional reason
    def part(channel, reason = '')
      @irc.part(channel, reason)
    end
    
    def names(channel)
      @irc.names(channel)
    end
    
    def kick(channel, nick, reason = nil)
      @irc.kick(nick, channel, reason)
    end
    
    def silence(time)
      if time > 0
        @silenced = true
        Thread.new { sleep time; @silenced = false; }
      end
    end
    
    def silenced?
      @silenced
    end
    
    def reload
      @modules.each { |mod| mod.reload }
    end
    
  end
end
