class Braces < AbstractModule

  def initialize(bot)
    super(bot)
  end

  def reload
    load __FILE__
  end

  def incoming_channel(fullactor, actor, target, text)
    # placeholder voor haakjesteller
  end

end