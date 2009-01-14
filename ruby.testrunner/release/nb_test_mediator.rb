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
require 'test/unit/ui/testrunnermediator'
require 'getoptlong'
require 'rubygems'
require 'rake'
require File.join(File.expand_path(File.dirname(__FILE__)), "nb_suite_runner")

class NbTestMediator

  def parse_args
    @suites = []

    parser = GetoptLong.new
    parser.set_options(
      ["-f", "--file", GetoptLong::OPTIONAL_ARGUMENT],
      ["-d", "--directory", GetoptLong::OPTIONAL_ARGUMENT],
      ["-m", "--testmethod", GetoptLong::OPTIONAL_ARGUMENT]
    )
    
    loop do
      begin
        opt, arg = parser.get
        break if not opt
        case opt
        # single file
        when "-f"
          add_to_suites [arg]
        # directory
        when "-d"
          add_to_suites Rake::FileList["#{arg}/**/test*.rb", "#{arg}/**/*test.rb"]
        # single test method
        when "-m"
          if "-m" != ""
            @suites.each do |s|
              tests_to_delete = []
              s.tests.each do |t|
                # t.method_name == "test: #{arg} for shoulda tests
                unless t.method_name == arg || t.method_name == "test: #{arg}. "
                  tests_to_delete << t
                end
              end
              tests_to_delete.each do |t|
                s.delete(t)
              end
            end
          end
        end
      end
    end
  end

  def require_file file
    begin
      file_name = file[0..file.length - 4]
      require "#{file_name}"
    rescue => err
      puts "%TEST_LOGGER% level=WARNING msg=Failed to load #{file}, error=#{err}"
      raise err
    end
  end

  def add_to_suites files
    t1 = Time.now
    # collect classes that extend TestCase/Suite before loading the files we are going to test
    # TODO: possibly not needed
    original_testcase_subclasses = Array.new(Test::Unit::TestCase::SUBCLASSES)
    original_testsuite_subclasses = Array.new(Test::Unit::TestSuite::SUBCLASSES)

    t2 = Time.now
    files.each do |file|
      require_file file
    end
    puts "%TEST_LOGGER% level=FINE msg=Loading #{files.size} files took #{Time.now - t2}"
    
    # we are interested only in test cases / suites we just loaded
    testcase_subclasses = Test::Unit::TestCase::SUBCLASSES - original_testcase_subclasses
    testsuite_subclasses = Test::Unit::TestSuite::SUBCLASSES - original_testsuite_subclasses
    
    # collect ancestors
    testcase_ancestors = []
    testcase_subclasses.each do |testcase|
      testcase_ancestors += testcase.ancestors.reject do |ancestor|  
        ancestor == testcase
      end
    end
    
    testsuite_ancestors = []
    testsuite_subclasses.each do |testsuite|
      testsuite_ancestors += testsuite.ancestors.reject do |ancestor|  
        ancestor == testsuite
      end
    end
    
    # reject test cases / suites that have subclasses (its test
    # are run when the subclass is run)
    testcase_subclasses.reject! do |testcase|
      testcase_ancestors.include?(testcase)
    end
    testsuite_subclasses.reject! do |testsuite|
      testsuite_ancestors.include?(testsuite)
    end
    
    # collects suites 
    testcase_subclasses.each do |testcase|
      @suites << testcase.suite
    end
    testsuite_subclasses.each do |testsuite|
      @suites << testsuite
    end
    
    # reject suites that contain no tests or only the default test. prevents
    # running of base test classes that are not used
    @suites.reject! do |suite|
      if suite.empty?
        true
      elsif suite.tests.length == 1
        "default_test(#{suite})" == (suite.tests[0].name)
      else         
        false
      end
    end
    
   puts "%TEST_LOGGER% level=FINE msg=Collected #{@suites.size} suites in #{Time.now - t1}"

  end

  def run_mediator
    parse_args
    suite_runner = NbSuiteRunner.new
    suite_runner.run(@suites)
  end
  
end

module Test
  module Unit
    class TestCase
      SUBCLASSES = []
      class << self
        #see IZ 150626
        alias_method :nb_mediator_existing_inherited, :inherited unless
          instance_methods.include?("nb_mediator_existing_inherited")
        def inherited(subclass)
          nb_mediator_existing_inherited(subclass)
          SUBCLASSES << subclass
          super
        end
      end
    end
  end
end

module Test
  module Unit
    class TestSuite
      SUBCLASSES = []
      class << self
        #see IZ 150626
        alias_method :nb_mediator_existing_inherited, :inherited unless
          instance_methods.include?("nb_mediator_existing_inherited")
        def inherited(subclass)
          nb_mediator_existing_inherited(subclass)
          SUBCLASSES << subclass
          super
        end
      end
    end
  end
end
  
NbTestMediator.new.run_mediator
