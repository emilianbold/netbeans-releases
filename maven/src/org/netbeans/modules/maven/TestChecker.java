/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.maven;

import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.spi.project.ActionProvider;

/**
 *
 * @author mkleint
 */
public class TestChecker implements PrerequisitesChecker {

    /**
     * Skip test execution.
     * Do not use maven.test.skip as that skips also compilation; see #189466 for background.
     * http://maven.apache.org/plugins/maven-surefire-plugin/examples/skipping-test.html
     */
    public static final String PROP_SKIP_TEST = "skipTests"; // NOI18N

    public TestChecker() {
    }

    public boolean checkRunConfig(RunConfig config) {
        String action = config.getActionName();
        if (ActionProvider.COMMAND_TEST.equals(action) ||
            ActionProvider.COMMAND_TEST_SINGLE.equals(action) ||
            ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(action) ||
            "profile-tests".equals(action)) { //NOI18N - profile-tests is not really nice but well.
            if (!RunUtils.hasTestCompileOnSaveEnabled(config)) {
                String test = config.getProperties().get("test");
                String method = config.getProperties().get(DefaultReplaceTokenProvider.METHOD_NAME);
                if (test != null && method != null) {
                    config.setProperty(DefaultReplaceTokenProvider.METHOD_NAME, null);
                    config.setProperty("test", test + '#' + method);
        }
            }
        } else if (MavenSettings.getDefault().isSkipTests()) {
            if (config.getPreExecution() != null) {
                checkRunConfig(config.getPreExecution());
            }
            if (config.getProperties().get(PROP_SKIP_TEST) == null) {
                config.setProperty(PROP_SKIP_TEST, "true"); //NOI18N
            }
        }
        return true;
    }

}
