require 'rubygems'
require 'net/yail'

require 'wrappers'
require 'handlers'

module Commode

  class Bot
    BOTNAME    = 'Commode bot'
    BOTVERSION = 'v0.0.1'

    EVENTS = ["incoming_welcome",    # After MOTD
              "incoming_invite",     # /INVITE'd to a channel
              "incoming_msg",        # Channel or private message
              "incoming_join",       # User joined channel
              "incoming_part",       # User left channel
              "incoming_quit",       # User quit
              "incoming_kick",       # User kicked from channel
              "incoming_nick"]       # User changed nick

    attr_reader   :irc
    attr_reader   :server, :port, :channels, :nick, :realname
    attr_accessor :modules

    include Wrappers
    include Handlers

    # Creates a new bot.  Note that due to my laziness, the options here
    # are almost exactly the same as those in Net::YAIL.  But at least there
    # are more defaults here.
    #
    # Options:
    # * <tt>:server</tt>: Name/IP of the IRC server
    # * <tt>:channels</tt>: Channels to automatically join on connect
    # * <tt>:port</tt>: Port number, defaults to 6667
    # * <tt>:nicks</tt>: Array of nicknames to cycle through
    # * <tt>:realname</tt>: Real name reported to server
    def initialize(options = {})
      @start_time = Time.now

      @servers  = options[:servers]
      @nextserver = 0
      @port     = options[:port]     || 6667
      @nicks    = options[:nicks]    || ['IRCBot1', 'IRCBot2', 'IRCBot3']
      @nick     = @nicks.first
      @realname = options[:realname] || 'IRCBot'
      @channels = options[:channels] || []

      @modules  = []

      self.connect_socket
      self.start_listening
    end

    # Tells us the main app wants to just wait until we're done with all
    # thread processing, or get a kill signal, or whatever.  For now this is
    # basically an endless loop that lets the threads do their thing until
    # the socket dies.
    def irc_loop
      while true
	until @irc.dead_socket
	  sleep 15
	  @irc.handle(:irc_loop)
	  Thread.pass
	end

	# Disconnected?  Wait a little while and start up again.
	@nextserver = (@nextserver+1) % @servers.length
	sleep 30
	@irc.stop_listening
	self.connect_socket
	start_listening
      end
    end

    protected

    # Sets up a socket and a handler to join channels specified
    def connect_socket
      @irc = Net::YAIL.new(
	:address    => @servers[@nextserver],
	:port       => @port,
	:username   => @nick,
	:realname   => @realname,
	:nicknames  => @nicks,
	:silent     => false,
	:loud       => false
      )

      # Simple hook for welcome to allow auto-joining of the channel
      EVENTS.each do |event|
	@irc.prepend_handler(event.to_sym,
			     self.method(("handle_" + event).to_sym))
      end
    end

    # Enters the socket's listening loop(s)
    def start_listening
      # If socket's already dead (probably couldn't connect to server), don't
      # try to listen!
      if @irc.dead_socket
	$stderr.puts "Dead socket, can't start listening!"
      end

      @irc.start_listening
    end

  end

end
