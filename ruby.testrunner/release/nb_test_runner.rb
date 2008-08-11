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

# Test runner that can be used with the rake test task.
class NbTestRunner
  extend Test::Unit::UI::TestRunnerUtilities

  def initialize(suite, output_level=NORMAL, io=STDOUT)
    if (suite.respond_to?(:suite))
      @suite = suite.suite
    else
      @suite = suite
    end
  end

  # Begins the test run.
  def start
    return run_mediator(suites)
  end

  # gets the suites to run
  def suites
    suites = []
    @suite.tests.each do |test|
      if test.kind_of? Test::Unit::TestSuite
        suites << test
      elsif (test.respond_to?(:suite))
        suites << test.suite
      end
    end
    suites.reject! do |suite|
      if suite.empty?
        true
      elsif suite.tests.length == 1
        "default_test(#{suite})" == (suite.tests[0].name)
      else
        false
      end
    end
    suites
  end

  # run the test mediator for the given suites
  def run_mediator(suites)
    start_suite_timer
    suites.each do |suite|
      @mediator = Test::Unit::UI::TestRunnerMediator.new(suite)
      attach_listeners
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
      stacktrace = result.exception.backtrace.join("%BR%")
      $stdout.print "%TEST_ERROR% time=#{elapsed_time} testname=#{result.test_name} message=#{result.message.to_s.gsub($/, " ")} location=#{stacktrace}\n"
    end
  end
  
  def test_result_changed(result)
  end
  
  def suite_started(result)
  end

  def suite_finished(result)
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

# Forces AutoRunner to use our test runner
module Test
  module Unit
    class AutoRunner
      def run
        Unit.run = false
        @suite = @collector[self]
        result = NbTestRunner
        Dir.chdir(@workdir) if @workdir
        result.run(@suite, @output_level)
        true
      end
    end
  end
end

require 'rubygems'
require 'rake'
require 'rake/tasklib'
require 'rake/testtask'

# adds our test runner to the require path for rake
module Rake
  class TestTask < TaskLib
    alias original_define define
    def define
      test_runner = ENV['NB_TEST_RUNNER']
      # convert the path to use forward slashes, backslashes
      # don't work here with JRuby. On MRI both work.
      test_runner = test_runner.gsub("\\", "/")
      @ruby_opts << "-r\"#{test_runner}\""
      original_define
    end
    
  end
end