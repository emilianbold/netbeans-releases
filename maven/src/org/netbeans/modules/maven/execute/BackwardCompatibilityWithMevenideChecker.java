/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class BackwardCompatibilityWithMevenideChecker implements PrerequisitesChecker {
    private static Logger LOG = Logger.getLogger(BackwardCompatibilityWithMevenideChecker.class.getName());

    public boolean checkRunConfig(RunConfig config) {
        String[] gls = config.getGoals().toArray(new String[0]);
        boolean changed = false;
        for (int i = 0; i < gls.length; i++) {
            if (gls[i].startsWith("org.codehaus.mevenide:netbeans-deploy-plugin")) {
                gls[i] = null;
                config.setProperty("netbeans.deploy", "true");
                changed = true;
                LOG.info("Dynamically removing netbeans-deploy-plugin goal from execution. No longer supported.");
            } else
            if (gls[i].startsWith("org.codehaus.mevenide:netbeans-nbmreload-plugin")) {
                gls[i] = null;
                LOG.info("Dynamically removing netbeans-nbmreload-plugin goal from execution. No longer supported.");
                changed = true;
            } else
            if (gls[i].startsWith("org.codehaus.mevenide:netbeans-run-plugin")) {
                gls[i] = null;
                LOG.info("Dynamically removing netbeans-run-plugin goal from execution. No longer supported.");
                changed = true;
            } else
            if (gls[i].startsWith("org.codehaus.mevenide:netbeans-debugger-plugin")) {
                gls[i] = null;
                LOG.info("Dynamically removing netbeans-debugger-plugin goal from execution. No longer supported.");
                changed = true;
            }
        }
        if (changed) {
            List<String> lst = config.getGoals();
            try {
                lst.clear();
                lst.addAll(Arrays.asList(gls));
                lst.remove(null);
            } catch (UnsupportedOperationException x) {
                Exceptions.printStackTrace(x);
            }
        }
        return true;
    }

}
