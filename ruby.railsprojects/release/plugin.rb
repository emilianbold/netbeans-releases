# 
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
# Copyright 2008 Sun Microsystems, Inc. All rights reserved.
# 
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
# particular file as subject to the "Classpath" exception as provided
# by Sun in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
# 
# Contributor(s):
# 
# Portions Copyrighted 2008 Sun Microsystems, Inc.

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
    # can't just redefine and give an alias to the uninstall method since
    # commands/plugin can't be required here; it executes some code that
    # will throw an exception when executed at this point
    if :uninstall == sym
      remove_method :uninstall
    end
  end
end