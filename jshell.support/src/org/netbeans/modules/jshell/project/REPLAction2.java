/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.project;

import java.io.IOException;
import org.netbeans.modules.java.repl.*;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.jshell.env.Bundle;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellRegistry;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class REPLAction2 implements ProjectActionPerformer {

    @ActionID(category="Project", id="org.netbeans.modules.java.repl.REPLAction2")
    @ActionReference(path = "Menu/BuildProject", separatorBefore = 93, position = 95)
    @ActionRegistration(displayName="#DN_ProjectJavaRun")
    @Messages({
        "DN_ProjectJavaRun=Run Java Shell"
    })
    public static Action create() {
        return ProjectSensitiveActions.projectSensitiveAction(new REPLAction2(), Bundle.DN_ProjectJavaRun(), null);
    }

    @Override
    public boolean enable(Project project) {
        for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (Utils.isNormalRoot(sg)) {
                if (Utils.findPlatform(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.BOOT)) != null)
                    return true;
            }
        }
        return false;
    }

    @Override
    public void perform(Project project) {
        JShellEnvironment env;
        try {
            env = ShellRegistry.get().openProjectSession(project);
            env.open();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
