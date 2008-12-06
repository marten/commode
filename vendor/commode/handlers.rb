module Commode
  module Handlers

    # Basic handler for joining our channels upon 
    # successful registration
    def handle_incoming_welcome(text, args)
      @channels.each do |channel| 
	@irc.join(channel)
	@irc.msg(channel, "oeps, verslapen o.0;;;; had hier een half uur geleden al moeten zijn")
      end
      return false
    end

    # TODO write something for this
    def handle_incoming_invite(*args);  false; end
    
    def handle_incoming_msg(fullactor, actor, channel, text);    
      if channel =~ /#{nick}/
	handle_incoming_private(fullactor, actor, channel, text)
      else
	handle_incoming_channel(fullactor, actor, channel, text)
      end
    end
   
    ["incoming_invite",     # /INVITE'd to a channel
     "incoming_channel",    # Channel message
     "incoming_private",    # Private message
     "incoming_join",       # User joined channel
     "incoming_part",       # User left channel
     "incoming_quit",       # User quit
     "incoming_kick",       # User kicked from channel
     "incoming_nick"        # User changed nick
    ].each do |method_name|
      eval <<-END
        def handle_#{method_name}(*args)
          @modules.each do |i|
	    begin
              i.#{method_name}(*args) if i.respond_to?(:#{method_name}) 
            rescue => e
	      handle_error(e, *args)
	    end
          end
        end
      END
    end

    # :nodoc:
    def handle_error(exception, *args)
      STDERR.puts("Exception for: " + args.inspect)
      STDERR.puts(exception.message)
      STDERR.puts(exception.backtrace.join("\n"))
    end

  end
end
