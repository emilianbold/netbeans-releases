def stale_session_check!
  yield
rescue ArgumentError => argument_error
  if argument_error.message =~ %r{undefined class/module ([\w:]+)}
    begin
      Module.const_missing($1)
    rescue LoadError, NameError => const_error
      raise ActionController::SessionRestoreError, <<-end_msg
Session contains objects whose class definition isn\'t available.
Remember to require the classes for all objects kept in the session.
(Original exception: #{const_error.message} [#{const_error.class}])
      end_msg
    end

    retry #1
  else
    raise
  end
end

retry #2

def foo
  retry #3
end

begin
  retry #4
rescue
  retry #5
end

