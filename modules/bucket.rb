class Bucket

  attr_accessor :probability, :master

  def initialize(bot)
    @bot = bot
    @brain = Hash.new([])
    @probability = 1
    @master = "marten"
  end

  def incoming_channel(fullactor, actor, target, text)
    case text
    when /^#{@bot.nick}[:,] vergeet (.*)/
      return unless actor == @master
      forget($1)
      @bot.send(target, "Ok, #{actor}. Laten we het er niet meer over hebben.")

    when /^#{@bot.nick}[:,] verander je naam in (.*)/
      return unless actor == @master
      @bot.nick = $1
    
    when /^#{@bot.nick}[:,] scheer je weg!/
      return unless actor == @master
      @bot.part(target)
    
    when /^#{@bot.nick}[:,] (.*) is ook (.*)/
      remember_also($1, $2)
      @bot.send(target, "Ok, #{actor}.")
    
    when /^#{@bot.nick}[:,] (.*) is (.*)/
      remember($1, $2)
      @bot.send(target, "Ok, #{actor}.")
    
    when /^#{@bot.nick}[:,] (stil (eens|jij)
			    |is je (numwis.|stage) al af\??)/x
      @talkfrom = Time.now + 60
      @bot.send(target, "Ok. Aan de numwis2 maar weer!")
    
    when /^#{@bot.nick}[:,] herhaal (.*)/
      response = recite($1)
      @bot.send(target, response) if response
    
    when /^#{@bot.nick}[:,] debug$/
      response = braindump
      @bot.send(target, response) if response
    
    when /^#{@bot.nick}[:,] herlaad/ 
      return unless actor == @master
      reload
      @bot.send(target, "Ok, #{actor}. Helemaal nieuw en glimmend.")
    
    when /^#{@bot.nick}[:,] (.*)/
      response = reply($1)
      @bot.send(target, response) if response
    
    else
      # zijn we gemute?
      if @talkfrom
	return if @talkfrom >= Time.now
	@talkfrom = nil
      end

      response = maybe { reply(text) } 
      @bot.send(target, response) if response
    end
  end

  def incoming_invite(fullactor, actor, target)
    return unless actor == @master
    @bot.join(target)
  end

  protected

  def remember(factoid, answer)
    @brain[factoid] = [answer]
  end

  def remember_also(factoid, answer)
    @brain[factoid] << answer
  end

  def recite(factoid)
    response = @brain[factoid].join('|')
    response.empty? ? "Dat doet geen belletje rinkelen." : response
  end

  def forget(factoid, answer = nil)
    if not answer
      @brain.delete(factoid)
    else
      @brain[factoid].delete(answer)
    end
  end
  
  def braindump
    @brain.inspect
  end

  def reply(possible_factoid)
    response = @brain.select() {|k,v| possible_factoid =~ /#{k}/ }
    response.at_rand()[1].at_rand
  end

  def maybe
    yield if rand() < @probability
  end

  def unescape(text)
    text.gsub!(/\\is/, "is")
  end

  def reload
    load __FILE__
  end
  
end
