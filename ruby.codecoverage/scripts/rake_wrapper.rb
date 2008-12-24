# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.
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

################################################################################
# rake_wrapper.rb
# @author: Tor Norbye <tor.norbye@sun.com>
#
# When we run rake, we need to trick rake into including rcov in the commands it
# launches to collect coverage. I couldn't find a great way to do this; for now,
# I achieve it by rewriting the Kernel.system call (!!).
#
# This code is fantastically inefficient and inelegant in how it processes the
# strings. I really don't know what I'm doing in Ruby string processing.
################################################################################

# A simple parser for splitting a plain string (foo bar "Hello World")
# into individual arguments ([foo, bar, Hello World]).
#
# This is a direct port of NetBeans'
#    org.openide.util.Utilities.parseParameters(String)
# in
#    openide.util/src/org/openide/util/Utilities.java
# and is probably not very Rubyesque.
#
class ParameterParser
  NULL = 0x0; # STICK + whitespace or NULL + non_"
  INPARAM = 0x1; # NULL + " or STICK + " or INPARAMPENDING + "\ // NOI18N
  INPARAMPENDING = 0x2; # INPARAM + \
  STICK = 0x4; # INPARAM + " or STICK + non_" // NOI18N
  STICKPENDING = 0x8; # STICK + \

  def self.parse(s)
    params = []
    state = NULL;
    buff = ""
    slength = s.length;

    for i in 0..slength-1
      c = s[i,1]

      if (c == ' ' || c == '\\t')
        if state == NULL
          if (buff.length > 0)
            # or use params += buff instead?
            params.push(buff)
            buff = ""
          end
        elsif state == STICK
          # or use params += buff instead?
          params.push(buff)
          buff = ""
          state = NULL
        elsif state == STICKPENDING
          buff += '\\'
          # or use params += buff instead?
          params.push(buff)
          buff = ""
          state = NULL
        elsif state == INPARAMPENDING
          state = INPARAM
          buff += '\\'
          buff += c
        else # INPARAM
          buff += c
        end

        next

      end

      if c == '\\'
        if state == NULL
          ++i
          if i < slength
            cc = s[i,1]
            if (cc == '"') || (cc == '\\')
              buff += cc
            elsif (cc == ' ') || (cc == '\\t')
              buff += c
              --i
            else
              buff += c
              buff += cc
            end
          else
            buff += '\\'
            break
          end

          next

        elsif state == INPARAM
          state = INPARAMPENDING
        elsif state == INPARAMPENDING
          buff += '\\'
          state = INPARAM
        elsif state == STICK
          state = STICKPENDING
        elsif state == STICKPENDING
          buff += '\\'
          state = STICK
        end

        next

      end

      if c == '"'
        if state == NULL
          state = INPARAM
        elsif state == INPARAM
          state = STICK
        elsif state == STICK
          state = INPARAM
        elsif state == STICKPENDING
          buff += '"'
          state = STICK
        else # INPARAMPENDING
          buff += '"'
          state = INPARAM
        end

        next
      end

      if state == INPARAMPENDING
        buff += '\\'
        state = INPARAM
      elsif state == STICKPENDING
        buff += '\\'
        state = STICK
      end

      buff += c
    end

    # Collect
    if state == INPARAM
      params.push(buff)
    elsif (state & (INPARAMPENDING | STICKPENDING)) != 0
      buff += '\\'
      params.push(buff)
    else # NULL or STICK
      if buff.length != 0
        params.push(buff)
      end
    end

    params
  end
end


module Kernel
  alias_method :netbeans_orig_system, :system

  #  alias :system_without_rcov :system
  RCOV = ENV['NB_RCOV_PATH']
  RCOV_ARGS = ENV['NB_RCOV_ARGS']


  def system(cmd, *rest)
    #puts "cmd before was #{cmd}"

    index = 0
    args = ParameterParser.parse(cmd)
    include_regexp = /^-I.*/
    preload_regexp = /^-r.*/

    ruby = ""
    includes = ""
    program_args = ""
    extra_scripts = ""
    script = ""

    ruby = args[0]
    found = false

    start=1
    if args[0].eql?("call") # Windows
      start=2
      ruby = 'call "' + args[1] + '"'
    end

    for i in start..args.length-1
      arg = args[i]
      if include_regexp =~ arg
        if ("-I".eql?(arg))
          includes << arg[i+1]
          i = i + 1
        else
          includes << arg
        end
        includes << " "
      elsif preload_regexp =~ arg
        if ("-r".eql?(arg))
          extra_scripts << '"'
          extra_scripts << args[i+1]
          extra_scripts << '"'
          i = i +1
        else
          extra_scripts << '"'
          extra_scripts << arg[2,arg.length]
          extra_scripts << '"'
        end
        extra_scripts << " "
      else
        if found
          program_args << " "
          program_args << arg
        else 
          script = arg
          found = true
        end
      end
      index = index+1
    end

    cmd = "#{ruby} \"#{RCOV}\" #{RCOV_ARGS} #{includes} \"#{script}\" #{extra_scripts} -- #{program_args}"

    #puts "After: ruby=#{ruby}, include=#{includes}, program_args=#{program_args}, extra_scripts=#{extra_scripts}, script=#{script}"
    #puts "Modified execution string to: #{cmd}"

    netbeans_orig_system(cmd, *rest)
  end
end

# Multiple -r's with rake doesn't seem to work, so make this script delegate instead!
NB_DELEGATED_SCRIPT = ENV['NB_DELEGATED_SCRIPT']
if (NB_DELEGATED_SCRIPT != nil)
  load NB_DELEGATED_SCRIPT
end
