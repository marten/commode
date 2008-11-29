module IRC
  class Channel

    def initialize(connection, channel)
      @conn = connection
      @conn.send "JOIN #{channel}"
    end

    def users
      # TODO
    end

  end
end
