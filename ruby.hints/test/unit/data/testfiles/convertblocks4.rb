      @scanner.set_prompt do
	|ltype, indent, continue, line_no|
	if ltype
	  f = @context.prompt_s
	elsif continue
	  f = @context.prompt_c
	elsif indent > 0
	  f = @context.prompt_n
	else @context.prompt_i
	  f = @context.prompt_i
	end
	f = "" unless f
	if @context.prompting?
	  @context.io.prompt = p = prompt(f, ltype, indent, line_no)
	else
	  @context.io.prompt = p = ""
	end
	if @context.auto_indent_mode
	  unless ltype
            ind = prompt(@context.prompt_i, ltype, indent, line_no)[/.*\z/].size +
	      indent * 2 - p.size
	    ind += 2 if continue
	    @context.io.prompt = p + " " * ind if ind > 0
	  end
	end
      end

def foo
  @context1.instance_eval do 
    @io = input_method 
    foobar 
    foo 
  end
  @context2.instance_eval { 
    @io = input_method 
    foobar 
    foo 
  }
  @context3.instance_eval { @io = input_method; foobar; foo }
  @context4.instance_eval do @io = input_method; foobar; foo end
end
