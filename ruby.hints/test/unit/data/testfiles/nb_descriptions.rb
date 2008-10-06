require 'yaml'
require 'rdoc/markup/simple_markup/fragments'

# BEGIN NETBEANS MODIFICATIONS
require 'set'
# END NETBEANS MODIFICATIONS


# Descriptions are created by RDoc (in nb_generator) and
# written out in serialized form into the documentation
# tree. ri then reads these to generate the documentation

module NB
  class NamedThing
    attr_reader :name
    def initialize(name)
      @name = name
    end
    def <=>(other)
      @name <=> other.name
    end

    def hash
      @name.hash
    end

    def eql?(other)
      @name.eql?(other)
    end
  end

  #  Alias          = Struct.new(:old_name, :new_name)

  class AliasName < NamedThing
  end

  class Attribute < NamedThing
    attr_reader :rw, :comment
    # BEGIN NETBEANS MODIFICATIONS
    attr_reader :rdoccomment
    # END NETBEANS MODIFICATIONS
    
    # BEGIN NETBEANS MODIFICATIONS
    #def initialize(name, rw, comment)
    def initialize(name, rw, comment, rdoccomment)
      # END NETBEANS MODIFICATIONS
      super(name)
      @rw = rw
      @comment = comment
      # BEGIN NETBEANS MODIFICATIONS
      @rdoccomment = rdoccomment
      # END NETBEANS MODIFICATIONS
    end
  end

  class Constant < NamedThing
    attr_reader :value, :comment
    # BEGIN NETBEANS MODIFICATIONS
    attr_reader :rdoccomment
    # END NETBEANS MODIFICATIONS
    
    # BEGIN NETBEANS MODIFICATIONS
    #def initialize(name, value, comment)
    def initialize(name, value, comment, rdoccomment)
      # END NETBEANS MODIFICATIONS
      super(name)
      @value = value
      @comment = comment
      # BEGIN NETBEANS MODIFICATIONS
      @rdoccomment = rdoccomment
      # END NETBEANS MODIFICATIONS
    end
  end

  
  
  class IncludedModule < NamedThing
  end

  # BEGIN NETBEANS MODIFICATIONS
  class Requires < NamedThing
  end
  class InFile < NamedThing
  end
  # END NETBEANS MODIFICATIONS

  class MethodSummary < NamedThing
    def initialize(name="")
      super
    end
  end



  class Description
    attr_accessor :name
    attr_accessor :full_name
    attr_accessor :comment

    def serialize
      self.to_yaml
    end

    def Description.deserialize(from)
      YAML.load(from)
    end

    def <=>(other)
      @name <=> other.name
    end
    
    def append_comment(rdoccomment, s)
        if rdoccomment != nil
          rdoccomment.each_line { |line| 

            if !(/^#/ =~  line)
              # C comments seem to have 3 or 4 spaces too many
              if /^    / =~ line
                #line = line.gsub(/^    /, "#")
                # TODO: How do I strip off 4?
                line = "#" + line[3, line.length-3]
                #line = "# " + line
              elsif /^   / =~ line
                #line = line.gsub(/^    /, "#")
                # TODO: How do I strip off 4?
                line = "#" + line[2, line.length-2]
                #line = "# " + line
              else
                line = "# " + line
              end
            end
            s << line
          }
          # Ensure the comment doesn't disable the next output
          if (!(/\n$/ =~ s))
            s << "\n"
          end
        end
    end
  end
  
  class ModuleDescription < Description
    
    attr_accessor :class_methods
    attr_accessor :instance_methods
    attr_accessor :attributes
    attr_accessor :constants
    attr_accessor :includes
    # BEGIN NETBEANS MODIFICATIONS
    attr_accessor :in_files
    attr_accessor :requires
    attr_accessor :rdoccomment
    
    def codename
      "module"
    end

    def extend
      nil
    end
    # END NETBEANS MODIFICATIONS

    # merge in another class desscription into this one
    def merge_in(old)
      merge(@class_methods, old.class_methods)
      merge(@instance_methods, old.instance_methods)
      merge(@attributes, old.attributes)
      merge(@constants, old.constants)
      merge(@includes, old.includes)
      if @comment.nil? || @comment.empty?
        @comment = old.comment
      else
        unless old.comment.nil? or old.comment.empty? then
          @comment << SM::Flow::RULE.new
          @comment.concat old.comment
        end
      end
    end

    def display_name
      "Module"
    end

    # the 'ClassDescription' subclass overrides this
    # to format up the name of a parent
    def superclass_string
      nil
    end


    # BEGIN NETBEANS MODIFICATIONS
    
    # Preferred filename
    def filename
      only_ext = true
      relative = nil
      if (@in_files != nil)
        @in_files.each { |file| 
          re = /ext\/(.*)+\/([^)]+)\.c/
          md = re.match(file.name)
          if md == nil
            only_ext = false
          else
            relative = md[1]
          end
        }
        if only_ext
          return relative
        end
      end
      
      nil
    end
    
    def stubify
      s = ""

      #      if (@in_files != nil)
      #        s << "# Defined in: \n"
      #        @in_files.each { |file| 
      #          s << "# "
      #          s << file.name
      #          s << "\n"
      #        }
      #      end

      if (@requires != nil)
        @requires.each { |require| 
          s << "require '"
          s << require.name
          s << "'\n"
        }
      end
      
      append_comment(@rdoccomment, s)
      
      s << codename
      s << " "
      s << @name
      extendCode = extend()
      if (extendCode != nil)
        s << extendCode
      end
      s << "\n"
      
      if (@includes != nil)
        @includes.each { |include|
          s << "  include "
          s << include.name
          s << "\n"
        }
      end
          
      if (@attributes != nil)
        @attributes.each { |attribute| 
          accessor = case attribute.rw
          when "RW" then "attr_accessor"
          when "R" then "attr_reader"
          when "W" then "attr_writer"
          else "#unknown attribute "
          end
          s << "  "
          s << accessor
          s << " :"
          s << attribute.name
          s << "\n"
        }
      end
      
      if (@constants != nil)
        @constants.each { |constant| 
          s << "  "
          key = constant.name
          # Some keys aren't compilable - rename these
          if (key == "END")
              key = "END_"
          end
          s << key
          s << " = "
          if (constant.value != nil)
            value = constant.value
            # Some values aren't compilable - turn those into strings
            if /\(/ =~ value
              value = "'" + value + "'"
            end
            s << value
          else 
            s << "unknown"
          end
          s << "\n"
        }
      end
      
      # TODO - class methods and instance methods?
      # These seem to be empty even for classes that have them      
      #      if (@class_methods != nil)
      #        @class_methods.each { |method| 
      #          s << "\n"
      #          s << method.stuify
      #          s << "\n"
      #        }
      #      end
      #
      #      if (@instance_methods != nil)
      #        @instance_methods.each { |method| 
      #          s << "\n"
      #          s << method.stuify
      #          s << "\n"
      #        }
      #      end

      return s
    end
    # END NETBEANS MODIFICATIONS
    
    private

    def merge(into, from)
      names = {}
      into.each {|i| names[i.name] = i }
      from.each {|i| names[i.name] = i }
      into.replace(names.keys.sort.map {|n| names[n]})
    end
    
  end
  
  class ClassDescription < ModuleDescription
    attr_accessor :superclass
    # BEGIN NETBEANS MODIFICATIONS
    # TODO - emit stuff from parent too
    attr_accessor :rdoccomment

    def extend
      s = superclass_string()
      if (s != nil)
        " < " + s
      else
        nil
      end
    end
    def codename
      "class"
    end
    # END NETBEANS MODIFICATIONS

    def display_name
      "Class"
    end

    def superclass_string
      if @superclass && @superclass != "Object"
        @superclass
      else
        nil
      end
    end
  end


  class MethodDescription < Description
    
    attr_accessor :is_class_method
    attr_accessor :visibility
    attr_accessor :block_params
    attr_accessor :is_singleton
    attr_accessor :aliases
    attr_accessor :is_alias_for
    attr_accessor :params
    # BEGIN NETBEANS MODIFICATIONS
    attr_accessor :call_seq
    attr_accessor :rdoccomment
    # END NETBEANS MODIFICATIONS

    # BEGIN NETBEANS MODIFICATIONS
    def stubify
      #TODO @aliases, @is_alias_for
      #TODO @is_class_method
      #TODO @block_params

      # @aliases is always empty - rdoc seems to duplicate the method instead
      # and set its documentation to "# Alias for _name_"
      
      methods = Set.new
      if (@call_seq != nil)
        # File.fnmatch(arg1, arg2) => foo    -> fnmatch, (arg1, arg2)
        # but also don't freak out on "def shift -> (key, value)"
        re = /([^(.:=-]+)(\([^)]+\))(\s*(=>|->).*)?/
        
        args = nil
        @call_seq.each_line { |line|
          # Handle special case
          if (line =~ /gdbm.shift/)
            methodname = "shift"
            args = ""
            signature = "shift"
          else
            md = re.match(line)
            if md != nil
              methodname = md[1]             
              args = argify(md[2])
              signature = methodname + args
              if !signature.eql?('set_trace_func(nil)') # Skip this method, it's just doc on nil behavior
                methods.add(signature)
              end
            end
          end
        }
        
        if methods.empty?
          # Perhaps the signature doesn't include parens, for example
          #  mtch.postmtch => result
          # Just use @name for this - works best for operators (===) and such
          methods.add(@name)
        end
      else
        signature = @name
        if @params == nil
          signature = @name
        elsif (@params.eql?('(...)'))
          # It's a method defined in native code that didn't include
          # a call-seq. That makes it an unknown - we'll just use *args
          signature = @name+"(*args)"
        elsif !@params.eql?("()")
          signature = signature + @params
        end
        methods.add(signature)
      end

      s = ""
      methods.each { |signature| 
        if @call_seq != nil
          @call_seq.each_line { |line| 
            if !(/^#/ =~  line)
              line = "#     " + line
            end
            s << line
          }
        end
        append_comment(@rdoccomment, s)
        if (@access != nil &&
          !@access.eql?("public"))
          s << @access + "\n"
        end
        s << "def "
        if @is_singleton
          s << "self."
        end
        
        s << signature
        s << "\n"
        s << <<FOO
    # This is just a stub for a builtin Ruby method.
    # See the top of this file for more info.
FOO
        s << "end\n\n"
      }

      return s
    end    

    #:arg:argList=>String
    def argify(argList)
      #        Clean up meta-syntax for arguments
      #        e.g.  initialize(string [, options [, lang]])
      #        should be initialize(string, options, lang)
      #        e.g.  union([pattern]*)
      #        should be union(pattern), etc.]
      #        e.g.  Struct.new( [aString] [, aSym]+> )
      #        shoudl be Struct.new( aString, aSym )
      #          def chomp!(separator=$/)
      #        should be
      #          def split(pattern=$;,limit)  ?
      #           delete!(other_str>)
      #         should be
      #        TODO: Handle " def []=( i1, i2, ... iN )"
      #
      #        Note that this "cleanup" will create signatures
      #        that do not reflect the original intent of the methods!
      #        I need to find better ways of writing these things,
      #        perhaps into a better code completion model aware of
      #        such conventions
      #
      #        Known special case in string.c:      
      if argList.eql?("(pattern=$;, [limit])")
        #            Can't just do the normal substitution here
        #            because I end up with
        #             (pattern="$;", limit)
        #            and that is illegal - you cannot have a default
        #            (for pattern) followed by a nondefault argument
        argList = "(pattern)";
      end
      if argList.eql?('(string=""[, mode])')
        return '(string="", mode=3)' # FMODE_READWRITE=3, the default
      end
      if argList.eql?('(obj, aProc=proc()')
        # The regexp doesn't include the last )
        return argList+")"
      end
      # Missing quotes
      argList = argList.gsub("=$/", "=\"$/\"");
      argList = argList.gsub("=$;", "=\"$;\"");
      argList = argList.gsub("=$,", "=\"$,\"");
      argList = argList.gsub("=$>", "=\"$>\"");

      # Prevent keywords in argument list
      #if (argList.eql?("(module, ...)")) {
      #   argList = "(modules)";
      #} else 
      if (/\(module, / =~ argList)
        argList = argList.gsub("(module, ", "(module_, ");
      end
      argList = argList.gsub(", end,", ", endd,"); # yuck
      argList = argList.gsub("]+>", "");
      argList = argList.gsub("]*", "");
      argList = argList.gsub("]+", "");
      argList = argList.gsub("]", "");
      argList = argList.gsub(" [", "");
      argList = argList.gsub("[", "");
      
      # TODO: ... probably means further args - put in an *args here?
      argList = argList.gsub("...", "");
      argList = argList.gsub("..", ""); #probably typo in original source

      # Compress spaces
      argList = argList.gsub("  ", " ");
      
      # Avoid keyword conflicts
      if (argList.eql?("(module)"))
        argList = "(module_name)";
      elsif (argList.eql?("(class)"))
        argList = "(class_name)";
      end
      
      argList = argList.gsub(", )", ")");
      argList = argList.gsub(",)", ")");
      argList = argList.gsub(">)", ")");
      argList = argList.gsub("//)", ")");
      argList = argList.gsub("()", "");
        
      # Some other known problems: Duplicate parameter names
      argList = argList.gsub("fixnum, fixnum", "fixnum1, fixnum2");
      
      return argList;
    end
    
    
    # END NETBEANS MODIFICATIONS
  end
  
end
