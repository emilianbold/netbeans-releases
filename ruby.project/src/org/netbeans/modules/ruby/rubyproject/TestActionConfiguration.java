/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject;

import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.openide.util.Parameters;

/**
 * A helper class for checking what test actions should be enabled.
 * See IZ 158940.
 *
 * @author Erno Mononen
 */
public final class TestActionConfiguration {

    /**
     * The prefix for the system properties disabling test actions.
     */
    private static final String COMMAND_PROPERTY_PREFIX = "ruby.test.actions."; //NOI18N
    /**
     * The project property for enabling/disabling test actions per project.
     */
    private static final String PROJECT_PROPERTY = "test.actions"; //NOI18N

    /**
     * Checks whether the action represented by the given <code>testCommand</code>
     * should be enabled in the context menu of the given <code>project</code>.
     * 
     * @param testCommand
     * @param project
     * @return true if the action should be enabled, false otherwise.
     */
    public static boolean enable(String testCommand, Project project) {
        Parameters.notEmpty("testCommand", testCommand);
        Parameters.notNull("project", project);

        PropertyEvaluator evaluator = project.getLookup().lookup(PropertyEvaluator.class);
        String propertyValue = evaluator != null ? evaluator.getProperty(PROJECT_PROPERTY) : null;
        if (propertyValue == null) {
            // if there is no project specific configuration, check whether
            // the command has been disabled using a system property
            return Boolean.parseBoolean(System.getProperty(COMMAND_PROPERTY_PREFIX + testCommand, "true")); //NOI18N
        }

        if ("".equals(propertyValue.trim())) { //NOI18N
            // empty test.actions, means all test actions are disabled for the project
            return false;
        }

        for (String each : propertyValue.split(",")) { //NOI18N
            if (testCommand.equals(each)) {
                return true;
            }
        }
        
        return false;
    }
}
