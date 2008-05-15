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
    example_groups.each do |example_group|
      start_time = Time.now
      puts "%SUITE_STARTING% #{example_group.description}"
      success = success & example_group.run
      elapsed_time = Time.now - start_time
      puts "%SUITE_FINISHED% #{example_group.description} time=#{elapsed_time}"
    end
    return success
  ensure
    finish
  end

  protected
  def reporter
    Reporter.new(@options)
  end

end

# TODO: probably would be better to use a formatter instead
class Reporter < Spec::Runner::Reporter

  def example_started(example)
    start_timer
    puts "%TEST_STARTED% #{example.description}"
    super
  end
      
  def failure(example, error)
    puts "%TEST_FAILED% #{example.description} time=#{elapsed_time}"
    super
  end
  alias_method :example_failed, :failure

  private
  def example_passed(example)
    puts "%TEST_FINISHED% #{example.description} time=#{elapsed_time}"
    super
  end
      
  def example_pending(example_group, example, message="Not Yet Implemented")
    puts "%TEST_PENDING% #{example.description}"
    super
  end
  
  def start_timer
    @start_time = Time.now
  end
  
  def elapsed_time
    Time.now - @start_time
  end

end
