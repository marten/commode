class Array
  def at_rand
    self[rand(length)]
  end
  
  def to_hash
    h = Hash.new
    self.each do |k,v|
      h[k] = v
    end
    h
  end
end
