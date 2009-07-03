class Bucket < AbstractModule

  include Commode::Plugins::HasMaster

  attr_accessor :probability, :master

  def initialize(bot)
    super(bot)

    @brain = Hash.new([])
    @probability = 0.2
    @master = "marten"

    load_brain rescue nil
  end

  def incoming_welcome(channel)
    respond_to_possible_factoid(channel, @bot.nick, "$welcome-#{channel}")
  end

  def incoming_channel(fullactor, actor, target, text)
    case text
    when /^#{@bot.nick}[:,] vergeet (.*)/
      return unless actor == @master
      forget($1)
      @bot.say(target, "Ok, #{actor}. Laten we het er niet meer over hebben.")
      
    when /^#{@bot.nick}[:,] (.*) \+?= (.*)/
      if actor == "vandread" || actor == "ijbema_"
        @bot.say(target, "Ja hallo, ik ga mezelf toch geen zinnen aanleren.")
        return
      end 

      remember($1, $2)
      @bot.say(target, "OK, sure.")
      # respond_to_possible_factoid(target, actor, " $reply-ok ")
      
    when /^#{@bot.nick}[:,] herhaal (.*)$/
      recite(target, actor, $1)
    
    when /^#{@bot.nick}[:,] debug$/
      puts "\n\nDEBUG\n"
      pp @brain
      puts "\n\n"

    when /^#{@bot.nick}[:,] !wtf/
      @bot.say(target, "#{actor}: reactie op ``#{@last_factoid_replied_to}''")

    when /^#{@bot.nick}[:,] set @probability ([01]\.\d+)$/
      return unless actor == @master
      @probability = $1.to_f
      respond_to_possible_factoid(target, actor, "$reply-ok $reply-set")
    
    when /^#{@bot.nick}[:,] (.*)/
      respond_to_possible_factoid(target, actor, text)
    
    else
      # zijn we gemute?
      return if @bot.silenced?
      if target == "#ijbema"
        respond_to_possible_factoid(target, actor, text)
      else
        maybe { respond_to_possible_factoid(target, actor, text) }
      end
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
        text.match(/(^|\s)#{k}(\s|$)/) rescue puts "ERROR"
      end
    end
    
    return if factoids.empty?
    
    factoids = factoids.to_hash
    # Then pick one factoid at random
    key = factoids.keys.at_rand()
    factoid = factoids[key]
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

    @last_factoid_replied_to = key
  end

  def remember(factoid, answer)
    if not @brain[factoid]
      @brain[factoid] = [answer]
    else
      @brain[factoid] << answer
    end

    save_brain
  end

  def recite(channel, nick, factoid, offset = 0)
    if match = factoid.match(/(.*)\[(\d+)\]$/)
      factoid = match[1]
      offset = match[2].to_i rescue 0
    end

    factoids = @brain[factoid]
    if (not factoids) or (factoids.empty?)
      @bot.say(channel, "#{nick}: die schiet me niet te binnen. misschien weet jan-eppo hem nog")
    else
      factoids[offset..offset+4].each_with_index do |obj, idx|
	@bot.say(channel, "#{factoid}[#{idx+offset}] = #{obj}")
      end
      if more = factoids[offset+5..-1]
	@bot.say(channel, "... en nog #{more.length}")
      end
    end
  end

  def forget(factoid, answer = nil)
    if not answer
      @brain.delete(factoid)
    else
      @brain[factoid].delete(answer)
    end
    
    save_brain
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

