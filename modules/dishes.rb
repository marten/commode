class Dishes < AbstractModule

  def initialize(bot)
    super(bot)
  end

  def reload
    load __FILE__
  end

  def incoming_channel(fullactor, nick, channel, text)
  end
  
  private
  
  
  
end