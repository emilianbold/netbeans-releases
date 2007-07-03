  def parse(argv, generators)
    old_argv = ARGV.dup
    begin
      ARGV.replace(argv)
      @op_dir = "doc"
      @op_name = nil
      @show_all = false
      @main_page = nil
      @marge     = false
      @exclude   = []
      @quiet = false
      @generator_name = 'html'
      @generator = generators[@generator_name]
      @rdoc_include = []
      @title = nil
      @template = nil
      @diagram = false
      @fileboxes = false
      @show_hash = false
      @image_format = 'png'
      @inline_source = false
      @all_one_file  = false
      @tab_width = 8
      @include_line_numbers = false
      @extra_accessor_flags = {}
      @promiscuous = false
      
      @css = nil
      @webcvs = nil
      
      @charset = case $KCODE
                 when /^S/i
                   'Shift_JIS'
                 when /^E/i
                   'EUC-JP'
                 else 
                   'iso-8859-1'
      
                 end
      accessors = []
      
      go = GetoptLong.new(*OptionList.options)
      go.quiet = true
      go.each do |opt, arg|
        case opt
        when "--all"           then @show_all      = true
        when "--charset"       then @charset       = arg
        when "--debug"         then $DEBUG         = true
        when "--exclude"       then @exclude       << Regexp.new(arg)
        when "--inline-source" then @inline_source = true
        when "--line-numbers"  then @include_line_numbers = true
        when "--main"          then @main_page     = arg
        when "--merge"         then @merge         = true
        when "--one-file"      then @all_one_file  = @inline_source = true
        when "--op"            then @op_dir        = arg
        when "--opname"        then @op_name       = arg
        when "--promiscuous"   then @promiscuous   = true
        when "--quiet"         then @quiet         = true
        when "--show-hash"     then @show_hash     = true
        when "--style"         then @css           = arg
        when "--template"      then @template      = arg
        when "--title"         then @title         = arg
        when "--webcvs"        then @webcvs        = arg
        
        when "--accessor" 
          arg.split(/,/).each do |accessor|
            if accessor =~ /^(\w+)(=(.*))?$/
              accessors << $1
              @extra_accessor_flags[$1] = $3
            end
          end
        
        when "--diagram"
          check_diagram
          @diagram = true
        
        end

      end

      @files = ARGV.dup

      @rdoc_include << "." if @rdoc_include.empty?

      if @exclude.empty?
        @exclude = nil
      else
        @exclude = Regexp.new(@exclude.join("|"))
      end

      check_files

      # If no template was specified, use the default
      # template for the output formatter

      @template ||= @generator_name

      # Generate a regexp from the accessors
      unless accessors.empty?
        re = '^(' + accessors.map{|a| Regexp.quote(a)}.join('|') + ')$'
        @extra_accessors = Regexp.new(re)
      end

    rescue GetoptLong::InvalidOption, GetoptLong::MissingArgument => error
      OptionList.error(error.message)

    ensure
      ARGV.replace(old_argv)
    end
  end


