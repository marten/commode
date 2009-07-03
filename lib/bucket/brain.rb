module Bucket
  class Brain
    
    attr_reader :memory
    
    def initialize
      @memory = Hash.new
    end
    
    def add(key, value)
      @memory[key] ||= Array.new
      @memory[key] = (@memory[key] << value)
    end
    
    def find(key)
      @memory[key] || []
    end
    
    def clear
      @memory = Hash.new
    end
    
    def phrases
      @memory.keys
    end
    
    def save(io)
      @memory.each do |key, values|
        values.each do |value|
          io.puts("#{key} = #{value}")
        end
      end
    end
    
    def self.load(io)
      brain = Brain.new
      while line = io.gets
        line.match(/^(.*)\s=\s(.*)$/)
        brain.add($1, $2)
      end
      brain
    end
  end
end
