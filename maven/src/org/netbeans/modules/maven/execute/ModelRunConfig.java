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

import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.openide.filesystems.FileObject;

/**
 * run configuration backed up by model
 * @author mkleint
 */
public final class ModelRunConfig extends BeanRunConfig {
    
    private NetbeansActionMapping model;
    
    /** Creates a new instance of ModelRunConfig */
    public ModelRunConfig(Project proj, NetbeansActionMapping mod, String actionName, FileObject selectedFile) {
        model = mod;
        NbMavenProjectImpl nbprj = proj.getLookup().lookup(NbMavenProjectImpl.class);
        setProject(nbprj);
        setExecutionName(nbprj.getName());
        setTaskDisplayName(nbprj.getName());
        setProperties(model.getProperties());
        setGoals(model.getGoals());
        setExecutionDirectory(ActionToGoalUtils.resolveProjectEecutionBasedir(mod, proj));
        setRecursive(mod.isRecursive());
        setActivatedProfiles(mod.getActivatedProfiles());
        setActionName(actionName);
        setFileObject(selectedFile);
        if (mod.getPreAction() != null) {
            setPreExecutionActionName(mod.getPreAction());
        }
        String react = mod.getReactor();
        if (react != null) {
            if ("am".equals(react) || "also-make".equals(react)) {
                setReactorStyle(ReactorStyle.ALSO_MAKE);
            } else if ("amd".equals(react) || "also-make-dependents".equals(react)) {
                setReactorStyle(ReactorStyle.ALSO_MAKE_DEPENDENTS);
            }
        }
    }

}
