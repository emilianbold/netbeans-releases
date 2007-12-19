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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui;

import java.io.File;
import java.text.MessageFormat;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.project.PhpProject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public final class SourceRootsUi {

    public static final String INVALID_SOURCE_ROOT_MSG = "MSG_InvalidSourceRoot"; // NOI18N
    public static final String INVALID_SOURCE_ROOT_TTL = "TTL_InvalidSourceRoot"; // NOI18N

    /**
     * checks if suggested source directory root is not occupied by another projects
     *
     * @param file File object of source root to verify
     * @param projectFolder project wich will contain this source root
     */
    public static boolean isRootNotOccupied(File file, File projectFolder) {
        Project p = FileOwnerQuery.getOwner(file.toURI());
        boolean isInsideProject = file.getAbsolutePath().startsWith(projectFolder.getAbsolutePath() + File.separatorChar);
        boolean isEqualToProject = file.getAbsolutePath().equalsIgnoreCase(projectFolder.getAbsolutePath());

        // actions inside this IF are copied from ruby project and adopted
        if (p != null && !isInsideProject && !isEqualToProject) {
            final Sources sources = (Sources) p.getLookup().lookup(Sources.class);
            if (sources == null) {
                return false;
            }
            final SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            final SourceGroup[] sourceGroupsPhp = sources.getSourceGroups(PhpProject.SOURCES_TYPE_PHP);
            final SourceGroup[] groups = new SourceGroup[sourceGroups.length + sourceGroupsPhp.length];
            System.arraycopy(sourceGroups, 0, groups, 0, sourceGroups.length);
            System.arraycopy(sourceGroupsPhp, 0, groups, sourceGroups.length, sourceGroupsPhp.length);
            final FileObject projectDirectory = p.getProjectDirectory();
            final FileObject fileObject = FileUtil.toFileObject(file);
            if (projectDirectory == null || fileObject == null) {
                return false;
            }
            for (int i = 0; i < groups.length; i++) {
                final FileObject sgRoot = groups[i].getRootFolder();
                if (fileObject.equals(sgRoot)) {
                    return false;
                }
                if (!projectDirectory.equals(sgRoot) && FileUtil.isParentOf(sgRoot, fileObject)) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public static void showSourceUsedDialog(File srcRoot) {
        String srcRootPath = srcRoot.getPath();
        
        Project p = FileOwnerQuery.getOwner(srcRoot.toURI());
        ProjectInformation pi = ProjectUtils.getInformation(p);
        String projectName = pi.getDisplayName();

        String msg = MessageFormat.format(NbBundle.getMessage(SourceRootsUi.class, INVALID_SOURCE_ROOT_MSG), srcRootPath, projectName);
        String title = NbBundle.getMessage(SourceRootsUi.class, INVALID_SOURCE_ROOT_TTL);

        NotifyDescriptor d = new NotifyDescriptor(msg, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(d);
    }
}
