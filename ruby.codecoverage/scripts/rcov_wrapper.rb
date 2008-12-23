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
# rcov_wrapper.rb
# @author: Tor Norbye <tor.norbye@sun.com>
#
# We run rcov, and when it is done, postprocess its
# output to write it in a simple format we can read (the native
# data written by rcov is marshalled ruby binary data structures
# which we don't want to parse directly since they are highly tied to
# the specific interpreter implementation.)
# TODO: Consider using metaprogramming to hook into rcov at a deeper level
# and directly modify the dumper routine such that we can emit the data
# in our desired format in the first place rather than writing, reading and
# rewriting as we're doing right now. This would also let us record some more
# information we'd be interested in, such as the inferred statement count.

state_file = ARGV.shift
output_file = ARGV.shift
rcovpath = ARGV.shift

# Register END block -before- running RCov to ensure that we run last
END {
  format, prev_state = File.open(state_file){|f| Marshal.load(f) }

  if (format.at(0) == 0 && format.at(1) == 1 && format.at(2) == 0)
    my_file = File.new(output_file, "w")
    prev_state.each_key do |filename|
      my_file.puts filename

      old_cov, old_counts = prev_state[filename].values_at(:coverage, :counts)

      for i in 0..old_counts.length-1
        type = old_cov.at(i)
        count = old_counts.at(i)
        if (type == :inferred)
          count = -1
        elsif !type
          count = -2
        end
        my_file.print "#{i}:#{count}, "
      end
      my_file.print "\n"
    end
    my_file.close
  else
    puts "Unsupported data format - " + format.to_s
  end
}

# Run rcov
load rcovpath
