require 'pp'

class Bucket < AbstractModule

  include Commode::Plugins::HasMaster

  attr_accessor :probability, :master

  def initialize(bot)
    super(bot)

    @brain = Hash.new([])
    @probability = 0.3
    @master = "marten"

    load_brain rescue nil
    Thread.new { while true; sleep 120; save_brain; end }
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

    when /^#{@bot.nick}[:,] kom je ook naar (\#.*)/
      @bot.join($1)
      @bot.send(target, "Zie je daar!")
    
    when /^#{@bot.nick}[:,] (.*) \+= (.*)/
      remember_also($1, $2)
      @bot.send(target, "Ok, #{actor}.")
    
    when /^#{@bot.nick}[:,] (.*) = (.*)/
      remember($1, $2)
      @bot.send(target, "Ok, #{actor}.")
    
    when /^#{@bot.nick}[:,]\s(stil\seens|
			      stil\sjij|
			      ga\sdood|
			      sterf|
			      donder\sop|
			      is\sje\snumwis2\sal\saf\??)/x
      @talkfrom = Time.now + 300
      @bot.send(target, "Ok. Aan de numwis2 maar weer!")
    
    when /^#{@bot.nick}[:,] herhaal (.*)/
      response = recite($1)
      @bot.send(target, response) if response
    
    when /^#{@bot.nick}[:,] debug$/
      puts "\n\nDEBUG\n"
      pp @brain
      puts "\n\n"
    
    when /^#{@bot.nick}[:,] herlaad/ 
      return unless actor == @master
      reload
      @bot.send(target, "Ok, #{actor}. Helemaal nieuw en glimmend.")
    
    when /^#{@bot.nick}[:,] (.*)/
      response_lines = reply(text).split("\n") 
      response_lines.each {|response| @bot.send(target, response) } if response_lines
    
    else
      # zijn we gemute?
      if @talkfrom
	return if @talkfrom >= Time.now
	@talkfrom = nil
      end

      response_lines = maybe { reply(text).split("\n") } 
      response_lines.each {|response| @bot.send(target, response) } if response_lines
    end
  end

  def incoming_invite(fullactor, actor, target)
    return unless actor == @master
    @bot.join(target)
  end

  def incoming_join(fullactor, actor, target)
    response = @brain["join-" + actor]
    @bot.send(target, response.at_rand) if response
  end

  def incoming_part(fullactor, actor, target, message)
    response = @brain["part-" + actor]
    @bot.send(target, response.at_rand) if response
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
  
  def reply(possible_factoid)
    response = @brain.select() {|k,v| possible_factoid.include? k }
    return [] unless response and not response.empty?
    response.at_rand()[1].at_rand 
  end

  def maybe
    yield if rand() < @probability
  end

  def reload
    save_brain
    load __FILE__
  end

  def brain_file
   File.join(File.dirname(__FILE__), 'bucket-brain.yaml') 
  end

  def load_brain
    @brain = File.open(brain_file) { |f| YAML::load(f) }
  end

  def save_brain
    puts "Saving brain"
    File.open(brain_file, 'w') {|f| f.puts @brain.to_yaml}
  end
  
end
