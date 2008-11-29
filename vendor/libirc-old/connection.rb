module IRC
  class Connection

    attr_reader   :server
    attr_reader   :port
    attr_accessor :nick

    def initialize(server, port, nick, realname)
      @socket = TCPSocket.new(server, port)
      @socket.puts "NICK #{nick}"
      @socket.puts "USER #{nick} 0 * :#{realname}"
      
      @server, @port, @nick, @realname = server, port, nick, realname
      @channels = []
    end

    def send(str)
      # TODO Sanitize str
      @socket.puts(str)
    end

    def nick=(value)
      @nick = value
      @socket.puts "NICK #{value}"
    end

    def realname=(value)
      @realname = value
      @socket.puts "USER #{nick} 0 * :#{value}"
    end

    def join(channel)
      @channels << IRC::Channel.new(self, channel)
    end

  end
end
