require File.join(File.dirname(__FILE__), '..', 'spec_helper')

module Bucket
  describe Brain do
    
    it "should store phrases by key" do
      @brain = Brain.new
      @brain.add("...", "<reply>[$someone silently unzips his pants]")
      @brain.phrases.length.should == 1
    end
    
    it "should retrieve phrases by key" do
      @brain = Brain.new
      @brain.add("...", "<reply>[$someone silently unzips his pants]")
      @brain.find("...").should == ["<reply>[$someone silently unzips his pants]"]
    end
    
    it "should store multiple phrases for the same key" do
      @brain = Brain.new
      @brain.add("...", "<reply>[$someone silently unzips his pants]")
      @brain.add("...", "<reply>[$someone suddenly starting breathing heavily]")
      @brain.find("...").should == ["<reply>[$someone silently unzips his pants]",
                                    "<reply>[$someone suddenly starting breathing heavily]"]
    end
    
    it "should return an empty array for unknown keys" do
      @brain = Brain.new
      @brain.find("...").should == []
    end
    
    it "should forget phrases" do
      @brain = Brain.new
      @brain.add("...", "<reply>[$someone silently unzips his pants]")
      @brain.add("...", "<reply>[$someone suddenly starting breathing heavily]")
      @brain.clear
      @brain.find("...").should == []
      @brain.phrases.length.should == 0
    end
    
    it "should save itself to disk" do
      @brain = Brain.new
      @brain.add("...", "<reply>[$someone silently unzips his pants]")
      
      mockfile = mock("mockfile")
      mockfile.should_receive(:puts)
      @brain.save(mockfile)
    end
    
    it "should load itself from disk" do
      lines = ["... = <reply>[$someone silently unzips his pants]",
               "... = <reply>[$someone suddenly starting breathing heavily]"]
      mockfile = StringIO.new(lines.join("\n"))
      
      @brain = Brain.load(mockfile)
      @brain.find("...").should == ["<reply>[$someone silently unzips his pants]",
                                    "<reply>[$someone suddenly starting breathing heavily]"]
    end
    
    it "should save and load if key and/or value contains an = sign" do
      @brain = Brain.new
      @brain.add("joop = idiot", "hahaha so true")
      @brain.add("joop", "joop = idiot")
      @brain.add("jaap = idiot", "joop = idiot too")
      
      mockfile = StringIO.new
      @brain.save(mockfile)

      mockfile.rewind

      @brain2 = Brain.load(mockfile)
      @brain2.find("joop = idiot").should == ["hahaha so true"]
      @brain2.find("joop").should == ["joop = idiot"]
      @brain2.find("jaap = idiot").should == ["joop = idiot too"]
      
      @brain.memory.should == @brain2.memory
    end
    
  end
end