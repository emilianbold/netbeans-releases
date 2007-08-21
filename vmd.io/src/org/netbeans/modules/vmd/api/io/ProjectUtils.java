/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.util.Arrays;
import java.util.List;

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
        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
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
