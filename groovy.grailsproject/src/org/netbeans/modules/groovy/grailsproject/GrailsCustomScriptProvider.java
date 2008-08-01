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

package org.netbeans.modules.groovy.grailsproject;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public final class GrailsCustomScriptProvider {

    private final Project project;

    private GrailsCustomScriptProvider(Project project) {
        this.project = project;
    }

    public static GrailsCustomScriptProvider forProject(Project project) {
        return new GrailsCustomScriptProvider(project);
    }

    public List<String> getCustomScripts() {
        Set<String> commands = new HashSet<String>();
        String userHome = System.getProperty("user.home"); // NOI18N

        // $HOME/.grails/scripts
        FileObject userScripts = FileUtil.toFileObject(FileUtil.normalizeFile(
                new File(userHome, ".grails" + File.separator + "scripts")));
        if (userScripts != null) {
            loadScripts(userScripts, commands);
        }

        // $PROJECT/scripts
        FileObject projectScripts = project.getProjectDirectory().getFileObject("scripts"); // NOI18N
        if (projectScripts != null) {
            loadScripts(projectScripts, commands);
        }

        // $PROJECT/plugins/*/scripts
        FileObject plugins = project.getProjectDirectory().getFileObject("plugins"); // NOI18N
        if (plugins != null) {
            for (Enumeration<? extends FileObject> e = plugins.getChildren(false); e.hasMoreElements();) {
                FileObject file = e.nextElement();
                if (file.isFolder()) {
                    FileObject scripts = file.getFileObject("scripts"); // NOI18N
                    if (scripts != null) {
                        loadScripts(scripts, commands);
                    }
                }
            }
        }

        return new ArrayList<String>(commands);
    }

    private void loadScripts(FileObject dir, Set<String> commands) {
        for (Enumeration<? extends FileObject> e = dir.getChildren(false); e.hasMoreElements();) {
            FileObject file = e.nextElement();
            if (file.hasExt("groovy") && !file.getName().startsWith("_")) { // NOI18N
                // TODO normalize name in future, to eliminate dupl.
                commands.add(file.getName());
            }
        }
    }

}
