/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.hibernate.refactoring;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.OccurrenceItem;
import org.netbeans.modules.hibernate.service.HibernateEnvironment;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateMappingMovePlugin implements RefactoringPlugin {

    private MoveRefactoring refactoring;
    private Project project;

    public HibernateMappingMovePlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        // TODO: verify 
        return null;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        URL targetURL = refactoring.getTarget().lookup(URL.class);
        if (targetURL == null) {
            // TODO: return a Problem
            return null;
        }

        String targetPackageName = HibernateRefactoringUtil.getPackageName(targetURL);
        if (targetPackageName == null) {
            // TODO: return a Problem
            return null;
        }

        List<String[]> oldNewMappingResources = new ArrayList<String[]>();
        for (FileObject fo : refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
            if (project == null) {
                project = FileOwnerQuery.getOwner(fo);
            }

            if (fo.getMIMEType().equals(HibernateMappingDataLoader.REQUIRED_MIME)) {
                oldNewMappingResources.add(getOldNewMappingResourceName(fo, targetPackageName));
            }
        }

        // Get the configuration files
        HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
        List<FileObject> configFiles = env.getAllHibernateConfigFileObjects();
        if (configFiles.isEmpty()) {
            return null;
        }

        // TODO: have all the modifications in one transaction
        for (String[] oldNewResourcePair : oldNewMappingResources) {
            Map<FileObject, List<OccurrenceItem>> occurrences =
                    HibernateRefactoringUtil.getMappingResourceOccurrences(configFiles, oldNewResourcePair[0]);

            for (FileObject configFile : occurrences.keySet()) {
                List<OccurrenceItem> foundPlaces = occurrences.get(configFile);
                for (OccurrenceItem foundPlace : foundPlaces) {
                    HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(configFile,
                            oldNewResourcePair[0],
                            oldNewResourcePair[1],
                            foundPlace.getLocation(),
                            foundPlace.getText());
                    refactoringElements.add(refactoring, elem);
                }
            }
            refactoringElements.registerTransaction(new HibernateMappingRenameTransaction(occurrences.keySet(), oldNewResourcePair[0], oldNewResourcePair[1]));
        }

        return null;
    }

    public static String[] getOldNewMappingResourceName(FileObject fo, String targetPkgName) {
        String names[] = new String[2]; // [oldName, newName]
        Project project = FileOwnerQuery.getOwner(fo);
        SourceGroup[] grp = SourceGroups.getJavaSourceGroups(project);
        if (grp.length == 0) {
            return null;
        }

        String srcRoot = grp[0].getRootFolder().getPath();
        String oldPath = fo.getPath();
        String oldResource = oldPath.substring(srcRoot.length() + 1);
        String fileName = oldResource.substring(oldResource.lastIndexOf("/") + 1);

        names[0] = oldResource;
        names[1] = targetPkgName + "/" + fileName;

        return names;
    }
}
