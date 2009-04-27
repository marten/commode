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
      @bot.say(target, "Ok, #{actor}. Laten we het er niet meer over hebben.")
      
    when /^#{@bot.nick}[:,] (.*) \+?= (.*)/
      remember($1, $2)
      @bot.say(target, "wat ooit")
      
    when /^#{@bot.nick}[:,] herhaal (.*)/
      response = recite($1)
      @bot.say(target, response) if response
    
    when /^#{@bot.nick}[:,] debug$/
      puts "\n\nDEBUG\n"
      pp @brain
      puts "\n\n"
    
    when /^#{@bot.nick}[:,] (.*)/
      respond_to_possible_factoid(target, actor, text)
    
    else
      # zijn we gemute?
      return if @bot.silenced?
      maybe { respond_to_possible_factoid(target, actor, text) }

    end
  end

  def incoming_join(fullactor, actor, target)
    response = @brain["join-" + actor]
    @bot.say(target, response.at_rand) if response
  end

  def incoming_part(fullactor, actor, target, message)
    response = @brain["part-" + actor]
    @bot.say(target, response.at_rand) if response
  end

  def reload
    save_brain
    load __FILE__
    load_brain
  end

  protected
  
  def respond_to_possible_factoid(channel, nick, text)
    # First, find all possibly matching factoids
    factoids = @brain.select() do |k,v| 
      case k
      when /^\/(.*)\/$/
        text.match(Regexp.new($1))
      else
        text.include? k
      end
    end
    
    return if factoids.empty?
    
    factoids = factoids.to_hash
    # Then pick one factoid at random
    factoid = factoids[factoids.keys.at_rand()]
    # Then pick one response this factoid at random
    response = factoid.at_rand()
    # Split at linebreaks and send as multiple messages
    response_lines = response.split("\\n") 
    
    # SEND
    response_lines.each do |line|
      line.gsub!("$who", nick)
      line.gsub!("$someone") do |match|
        @names ||= @bot.names(channel)
        @names.at_rand
      end
      @names = nil
      
      case line
      when /^<reply>(.*)/
        @bot.say(channel, $1)
      when /^<action>(.*)/
        @bot.act(channel, $1)
      when /^<kick (.*)>(.*)/
        @bot.kick(channel, $1, $2)
      when /^<kick>(.*)/
        @bot.kick(channel, nick, $1)
      else
        @bot.say(channel, line)
      end
    end
  end

  def remember(factoid, answer)
    if not @brain[factoid]
      @brain[factoid] = [answer]
    else
      @brain[factoid] << answer
    end
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
  
  def maybe(chance = @probability)
    yield if rand() < chance
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

