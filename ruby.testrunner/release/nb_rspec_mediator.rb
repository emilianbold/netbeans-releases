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


class NbRspecMediator < Spec::Runner::ExampleGroupRunner

  def initialize(options, args)
    super(options)
  end

  def load_files(files)
    super(files)
  end

  def run
    prepare
    success = true
    if @options.line_number != nil
      @spec_parser = NbSpecParser.new
      @spec_parser.spec_name_for(@options.files[0], @options.line_number)
    end

    overall_start_time = Time.now
    example_groups.each do |example_group|
      if (@spec_parser != nil && example_group.description != @spec_parser.example_group_description)
        next
      end
      example_group_start_time = Time.now
      puts "%SUITE_STARTING% #{example_group.description}"
      success = success & example_group.run
      elapsed_time = Time.now - example_group_start_time
      puts "%SUITE_FINISHED% #{example_group.description} time=#{elapsed_time}"
    end
    @duration = Time.now - overall_start_time
    return success
  ensure
    reporter.duration = @duration
    finish
  end

  protected
  def reporter
    if @reporter.nil?
      @reporter = Reporter.new(@options)
    end
    @reporter
  end

end

# TODO: probably would be better to use a formatter instead
class Reporter < Spec::Runner::Reporter

  attr_accessor :duration
  def duration
    @duration
  end
  
  def example_started(example)
    start_timer
    puts "%TEST_STARTED% #{example.description}"
    super
  end
      
  def failure(example, error)
    backtrace_tweaker.tweak_backtrace(error)
    puts "%TEST_FAILED% #{example.description} time=#{elapsed_time} message=#{error.message} location=#{error.backtrace[0]}"
    super
  end
  alias_method :example_failed, :failure

  private
  def example_passed(example)
    puts "%TEST_FINISHED% #{example.description} time=#{elapsed_time}"
    super
  end
      
  def example_pending(example_group, example, message="Not Yet Implemented")
    puts "%TEST_PENDING% #{example.description} time=#{elapsed_time} #{message}"
    super
  end
  
  def start_timer
    @start_time = Time.now
  end
  
  def elapsed_time
    Time.now - @start_time
  end

end

class NbSpecParser < Spec::Runner::SpecParser
  
  attr_reader :example_group_description, :example_description
  
  def spec_name_for(file, line_number)
    best_match.clear
    file = File.expand_path(file)
    rspec_options.example_groups.each do |example_group|
      consider_example_groups_for_best_match example_group, file, line_number
      example_group.examples.each do |example|
        consider_example_for_best_match example, example_group, file, line_number
      end
    end
    if best_match[:example_group]
      @example_group_description = best_match[:example_group].description
      if best_match[:example]
        @example_description = best_match[:example].description
        "#{best_match[:example_group].description}  #{best_match[:example].description}"
      else
        best_match[:example_group].description
      end
    else
      nil
    end
  end
  
end
