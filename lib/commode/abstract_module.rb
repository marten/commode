class AbstractModule
  
  def initialize(bot)
    @bot = bot
  end
  
  # You can override this if you need to save settings to disk before reloading modules
  def reload
    # no-op
  end

end
