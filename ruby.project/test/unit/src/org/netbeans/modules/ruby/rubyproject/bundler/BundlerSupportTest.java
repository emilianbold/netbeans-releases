/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.rubyproject.bundler;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.ruby.rubyproject.bundler.BundlerSupport.Task;
import org.netbeans.modules.ruby.rubyproject.bundler.BundlerSupport.TaskCollector;
import static org.junit.Assert.*;

/**
 *
 * @author erno
 */
public class BundlerSupportTest {

    public BundlerSupportTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testTaskParser() {
        /* Sample bundler output:
         * 
        Tasks:
          bundle check        # Checks if the dependencies listed in Gemfile are satisfied by currently installed gems
          bundle exec         # Run the command in context of the bundle
          bundle help [TASK]  # Describe available tasks or one specific task
          bundle init         # Generates a Gemfile into the current working directory
          bundle install      # Install the current environment to the system
          bundle lock         # Locks the bundle to the current set of dependencies, including all child dependencies.
          bundle pack         # Packs all the gems to vendor/cache
          bundle show         # Shows all gems that are part of the bundle.
          bundle unlock       # Unlock the bundle. This allows gem versions to be changed
         */
         assertNull(TaskCollector.parse("Tasks:"));
         Task check = TaskCollector.parse("bundle check        # Checks if the dependencies listed in Gemfile are satisfied by currently installed gems");
         assertEquals("check", check.name);
         assertEquals("Checks if the dependencies listed in Gemfile are satisfied by currently installed gems", check.descriptor);

         Task help = TaskCollector.parse("bundle help [TASK]  # Describe available tasks or one specific task");
         assertEquals("help", help.name);
         assertEquals("Describe available tasks or one specific task", help.descriptor);
    }

}