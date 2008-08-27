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

require 'rubygems'
require 'autotest'


Autotest.add_hook :initialize do |at|
  load(__FILE__)
end

Autotest.add_hook :run_command do |at|
  puts "%AUTOTEST% reset"
end

if require 'autotest/rspec'
  # Loads NbRspecMediator for running specs.
  class Autotest::Rspec < Autotest
    #  remove_method :make_test_cmd
    alias old_rspec_mtc make_test_cmd
    # force spec to use our test mediator
    def make_test_cmd(files_to_test)
      options = ENV['NB_RSPEC_MEDIATOR']
      return "#{ruby} -S #{spec_command} #{options} #{files_to_test.keys.flatten.join(' ')}"
    end

    def hook(name)
      super
      # needs to done here instead of adding a hook -- that would
      # confuse the test/unit runner
      if name == :initialize
        # the lines below are from rspec-1.1.4/lib/autotest/rspec.rb
        self.clear_mappings
        # watch out: Ruby bug (1.8.6):
        # %r(/) != /\//
        self.add_mapping(%r%^spec/.*\.rb$%) { |filename, _|
          filename
        }
        self.add_mapping(%r%^lib/(.*)\.rb$%) { |_, m|
          ["spec/#{m[1]}_spec.rb"]
        }
        self.add_mapping(%r%^spec/(spec_helper|shared/.*)\.rb$%) {
          self.files_matching %r%^spec/.*_spec\.rb$%
        }
      end
    end
  end
end

# Loads NbTestRunner for test/unit tests
class Autotest
  remove_method :make_test_cmd
  def make_test_cmd files_to_test
    cmds = []
    full, partial = reorder(files_to_test).partition { |k,v| v.empty? }

    test_runner = ENV['NB_TEST_RUNNER']

    unless full.empty? then
      classes = full.map {|k,v| k}.flatten.uniq.join(' ')
      cmds << "#{ruby} -I#{libs} -r\"#{test_runner}\" -rtest/unit -e \"%w[#{classes}].each { |f| require f }\""
    end

    partial.each do |klass, methods|
      regexp = Regexp.union(*methods).source
      cmds << "#{ruby} -I#{libs} -r\"#{test_runner}\" -rtest/unit #{klass} -n \"/^(#{regexp})$/\""
    end

    res = cmds.join("#{SEP} ")
    return res
  end

end