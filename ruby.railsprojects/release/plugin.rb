# Opens the Rails plugin class so that the uninstall
# method does not actually remove the plugin folder; this
# left for the IDE -- see #142698
class Plugin

  def method_missing(meth, *args, &blk)
    return super unless meth == :uninstall
    do_uninstall
  end

  def do_uninstall
    # othewise identical to the original Plugin#uninstall method, 
    # just the rm_r path is commented out here
    path = "#{rails_env.root}/vendor/plugins/#{name}"
    if File.directory?(path)
      puts "Removing 'vendor/plugins/#{name}'" if $verbose
      run_uninstall_hook
      #      rm_r path -- the IDE should do this
    else
      puts "Plugin doesn't exist: #{path}"
    end
    # clean up svn:externals
    puts "externall.."
    externals = rails_env.externals
    externals.reject!{|n,u| name == n or name == u}
    rails_env.externals = externals
  end

  def Plugin.method_added(sym)
    if :uninstall == sym
      remove_method :uninstall
    end
  end
end