/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.project;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
public final class ProjectUtils {
    /**
     * Determines if the project wants to launch a JShell. 
     * @param p the project
     * @return true, if JShell support is enabled in the active configuration.
     */
    public static boolean isJShellRunEnabled(Project p) {
        J2SEPropertyEvaluator  prjEval = p.getLookup().lookup(J2SEPropertyEvaluator.class);
        return Boolean.parseBoolean(prjEval.evaluator().evaluate("${jshell.run.enable}"));
    }
    
    /**
     * Determines a Project given a debugger session. Acquires a baseDir from the
     * debugger and attempts to find a project which owns it. May return {@code null{
     * @param s
     * @return project or {@code null}.
     */
    public static Project getSessionProject(Session s) {
        Map m = s.lookupFirst(null, Map.class);
        if (m == null) {
            return null;
        }
        Object bd = m.get("baseDir"); // NOI18N
        if (bd instanceof File) {
            FileObject fob = FileUtil.toFileObject((File)bd);
            if (fob == null || !fob.isFolder()) {
                return null;
            }
            try {
                Project p = ProjectManager.getDefault().findProject(fob);
                return p;
            } catch (IOException | IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        return null;
    }
}
