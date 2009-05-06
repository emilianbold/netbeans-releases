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
 
require 'test/unit'
require 'test/unit/testcase'
require 'test/unit/testsuite'
require 'test/unit/autorunner'
require 'test/unit/ui/testrunnermediator'
require 'test/unit/ui/testrunnerutilities'

class NbSuiteRunner
  
  # Runs the given test suites.
  def run(suites)
    suites.each do |suite|
      @mediator = Test::Unit::UI::TestRunnerMediator.new(suite)
      attach_listeners
      start_suite_timer
      begin
        $stdout.print "%SUITE_STARTING% #{suite}\n"
        result = @mediator.run_suite
      rescue => err
        $stdout.print "%SUITE_ERROR_OUTPUT% error=#{err}\n"
      ensure
        $stdout.print "%SUITE_FINISHED% time=#{elapsed_suite_time}\n"
      end
    end
  end

  def start_suite_timer
    @suite_start_time = Time.now
  end
  
  def elapsed_suite_time
    Time.now - @suite_start_time
  end
  def attach_listeners
    @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::STARTED, &method(:suite_started))
    @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::FINISHED, &method(:suite_finished))
    @mediator.add_listener(Test::Unit::TestResult::CHANGED, &method(:test_result_changed))
    @mediator.add_listener(Test::Unit::TestResult::FAULT, &method(:test_fault))
    @mediator.add_listener(Test::Unit::TestCase::STARTED, &method(:test_started))
    @mediator.add_listener(Test::Unit::TestCase::FINISHED, &method(:test_finished))
  end
  
  def test_fault(result)
    if (result.instance_of?(Test::Unit::Failure))
      if (result.location.kind_of? Array)
        location = result.location.join("%BR%")
      else 
        location = result.location
      end
      $stdout.print "%TEST_FAILED% time=#{elapsed_time} testname=#{result.test_name} message=#{result.message.to_s.gsub($/, " ")} location=#{location}\n"
    else
      backtrace = []
      # in certain cases the whole stacktrace is just
      # a single string with newlines - see e.g. IZ 158581
      if result.exception.backtrace.size == 1
        result.exception.backtrace[0].split("\n").each do |line|
          # preserve indendation for (r)html things in the error stack trace
          # that are of the following format:
          #    1: <p>
          #    2:   <b>Foo:</b>
          #    3:   <%=h @bar.foo %>
          #    4: </p>
          #    5:
          unless line =~ /\s*\d:\s.*/
            line = line.lstrip
          end
          backtrace << line
        end
      else
        backtrace = result.exception.backtrace
      end
      $stdout.print "%TEST_ERROR% time=#{elapsed_time} testname=#{result.test_name} message=#{result.message.to_s.gsub($/, " ")} location=#{backtrace.join("%BR%")}\n"
    end
  end

  def test_result_changed(result)
    puts "%TEST_RESULT_CHANGED% #{result}"
  end

  def suite_started(result)
    puts "%SUITE_STARTED% #{result}"
  end

  def suite_finished(result)
    # handled in the main loop that runs suites
  end
  
  def test_started(result)
    start_timer
    $stdout.print "%TEST_STARTED% #{result}\n"
  end

  def test_finished(result)
    $stdout.print "%TEST_FINISHED% time=#{elapsed_time} #{result}\n"
  end
  
  def start_timer
    @start_time = Time.now
  end
  
  def elapsed_time
    Time.now - @start_time
  end
end
