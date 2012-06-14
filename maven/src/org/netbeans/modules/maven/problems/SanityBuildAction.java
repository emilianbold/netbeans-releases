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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.problems;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.AbstractAction;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.execute.RunConfig.ReactorStyle;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * Corrective action to run some target which can download plugins or parent POMs.
 * At worst it will show the same problem in the Output Window, so the user is more likely
 * to believe that there really is a problem with their project, not NetBeans.
 */
@Messages("ACT_validate=Priming Build")
public class SanityBuildAction extends AbstractAction {

    private final NbMavenProjectImpl nbproject;

    public SanityBuildAction(NbMavenProjectImpl nbproject) {
        super(ACT_validate());
        this.nbproject = nbproject;
    }

    public @Override void actionPerformed(ActionEvent e) {
        BeanRunConfig config = new BeanRunConfig();
        config.setExecutionDirectory(FileUtil.toFile(nbproject.getProjectDirectory()));
        config.setGoals(Arrays.asList("--fail-at-end", "install")); // NOI18N
        config.setReactorStyle(ReactorStyle.ALSO_MAKE);
        //after a discussion with jglick -> comment out, could add artifacts with failing tests into local repository
        // i'm unclear what the scope of the problem is, needs more investigation
        //config.setProperty(TestChecker.PROP_SKIP_TEST, "true"); //priming doesn't need test execution, just compilation
        config.setProject(nbproject);
        String label = build_label(nbproject.getProjectDirectory().getNameExt());
        config.setExecutionName(label);
        config.setTaskDisplayName(label);
        RunUtils.run(config);
    }

}
