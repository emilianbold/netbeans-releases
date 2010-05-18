/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.vmd.api.io;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author David Kaspar
 */
public final class ProjectUtils {
    
    private ProjectUtils() {
    }
    
    /**
     * Returns a project id for a project.
     * @param project the project
     * @return the project id
     * @throws NullPointerException when the project parameter is null
     */
    public static String getProjectID(Project project) {
        return FileUtil.toFile(project.getProjectDirectory()).toURI().toString();
    }
    
    /**
     * Returns a project instance for a project id.
     * @param projectID the project id
     * @return the project; null if not exists
     * @throws RuntimeException when project id has invalid format
     */
    public static Project getProject(String projectID) {
        try {
            return FileOwnerQuery.getOwner(new URI(projectID));
        } catch (URISyntaxException e) {
            throw Debug.error(e);
        }
    }
    
    /**
     * Returns a project instance for a context.
     * @param context the context
     * @return the project
     * @throws NullPointerException when the context parameter is null
     */
    public static Project getProject(DataObjectContext context) {
        if (context == null)
            return null;
        return FileOwnerQuery.getOwner (context.getDataObject().getPrimaryFile ());
    }
    
    /**
     * Returns a project instance for a data object.
     * @param document the document
     * @return the project
     * @throws NullPointerException when the DesignDocument parameter is null
     */
    public static Project getProject(DesignDocument document) {
        return getProject(getDataObjectContextForDocument(document));
    }
    
    /**
     * Returns a DataObjectContext for a specific document.
     * It founds a context only for those documents that are active and currently assigned to the context.
     * There is only one active document per each context. Documents, that are loading process, are not claimed at active.
     * @param document the document
     * @return the context; null if no context found
     */
    public static DataObjectContext getDataObjectContextForDocument(DesignDocument document) {
        return IOSupport.getDataObjectContextForDocumentInterface(document);
    }
    
    /**
     * Returns a list of SourceGroups for a specific data object context.
     *
     * @param context the data object context
     * @return the list of SourceGroups
     * @see org.netbeans.api.project.SourceGroup
     */
    public static List<SourceGroup> getSourceGroups(DataObjectContext context) {
        assert context != null;
        Project project = getProject(context);
        if (project == null) {
            return Collections.<SourceGroup>emptyList();
        }
        Sources sources = org.netbeans.api.project.ProjectUtils.getSources(project);
        SourceGroup[] sg = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        return Arrays.asList(sg);
    }
    
    /**
     * Returns a list of SourceGroups for a specific projectID.
     *
     * @param projectID the ID of prject
     * @return the list of SourceGroups
     * @see org.netbeans.api.project.SourceGroup
     */
    public static List<SourceGroup> getSourceGroups(String projectID) {
        assert projectID != null;
        Project project = getProject(projectID);
        if (project == null)
            return null;
        Sources sources = org.netbeans.api.project.ProjectUtils.getSources(project);
        SourceGroup[] sg = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        return Arrays.asList(sg);
    }
    /**
     *  RequestVisibity for TopComponent
     */
    public static void requestVisibility (DataObjectContext context, String topComponentDisplayName) {
        Set<TopComponent> topComponents = new HashSet<TopComponent>(TopComponent.getRegistry().getOpened());
        for (TopComponent tc : topComponents) {
            DataEditorView dev = tc.getLookup().lookup(DataEditorView.class);
            if (dev == null  ||  dev.getContext() != context)
                continue;
            MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
            for (MultiViewPerspective perspective : handler.getPerspectives()) {
                if (perspective.getDisplayName().equals(topComponentDisplayName)) {
                    handler.requestVisible(perspective);
                    break;
                }
            }
        }
    }
    
    /**
     * Returns a display name for source editor views
     * @return the display name
     */
    public static String getSourceEditorViewDisplayName () {
        return NbBundle.getMessage (ProjectUtils.class, "LBL_SourceEditorView"); // NOI18N
    }

}
