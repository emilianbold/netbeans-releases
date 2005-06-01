/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.system.cvss.ui.actions.checkout;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.cookies.InstanceCookie;
import org.openide.ErrorManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.Collections;
import java.io.IOException;
import java.io.File;

/**
 * Simpliied nb_all/projects/projectui/src/org/netbeans/modules/project/ui/ProjectUtilities.java,
 * nb_all/projects/projectui/src/org/netbeans/modules/project/ui/ProjectTab.java and
 * nb_all/ide/welcome/src/org/netbeans/modules/welcome/ui/TitlePanel.java copy.
 *
 * @author Petr Kuzel
 */
final class ProjectUtilities {

    private static final String ProjectTab_ID_LOGICAL = "projectTabLogical_tc"; // NOI18N

    private static final String NEW_PROJECT_ACTION = "Actions/Project/org-netbeans-modules-project-ui-NewProject.instance";  // NOI18N

    private static final String INITIAL_SOURCE_ROOT = "EXISTING_SOURCES_CURRENT_DIRECTORY";  // NOI!*N


    public static void selectAndExpandProject( final Project p ) {

        // invoke later to select the being opened project if the focus is outside ProjectTab
        SwingUtilities.invokeLater (new Runnable () {

            final ExplorerManager.Provider ptLogial = findDefault(ProjectTab_ID_LOGICAL);

            public void run () {
                Node root = ptLogial.getExplorerManager ().getRootContext ();
                // Node projNode = root.getChildren ().findChild( p.getProjectDirectory().getName () );
                Node projNode = root.getChildren ().findChild( ProjectUtils.getInformation( p ).getName() );
                if ( projNode != null ) {
                    try {
                        ptLogial.getExplorerManager ().setSelectedNodes( new Node[] { projNode } );
                    } catch (Exception ignore) {
                        // may ignore it
                    }
                }
            }
        });

    }

    /* Singleton accessor. As ProjectTab is persistent singleton this
     * accessor makes sure that ProjectTab is deserialized by window system.
     * Uses known unique TopComponent ID TC_ID = "projectTab_tc" to get ProjectTab instance
     * from window system. "projectTab_tc" is name of settings file defined in module layer.
     * For example ProjectTabAction uses this method to create instance if necessary.
     */
    private static synchronized ExplorerManager.Provider findDefault( String tcID ) {
        TopComponent tc = WindowManager.getDefault().findTopComponent( tcID );
        return (ExplorerManager.Provider) tc;
    }

    public static void newProjectWizard(File workingDirectory) {
        Action action = CommonProjectActions.newProjectAction();
        if (action != null) {
            // #58486 honored by j2seproject PanelSourceFolders.java
            FileObject workingFolder = FileUtil.toFileObject(workingDirectory);
            action.putValue(CommonProjectActions.EXISTING_SOURCES_FOLDER, workingFolder);
            performAction(action);
        }
    }

    /**
     * Scans given folder (and subfolder into deep 5) for projects.
     * @return List of {@link Project}s never <code>null</code>.
     */
    public static List scanForProjects(FileObject scanRoot) {
        return scanForProjectsRecursively(scanRoot, 5);
    }

    private static List scanForProjectsRecursively(FileObject scanRoot, int deep) {
        if (deep <= 0) return Collections.EMPTY_LIST;
        List projects = new LinkedList();
        ProjectManager projectManager = ProjectManager.getDefault();
        if (projectManager.isProject(scanRoot)) {
            try {
                Project prj = projectManager.findProject(scanRoot);
                projects.add(prj);
            } catch (IOException e) {
                // it happens for all apisupport projects unless
                // checked out into directory that contains nbbuild and openide folders
                // apisupport project is valid only if placed in defined directory structure
                ErrorManager.getDefault().annotate(e, "Ignoring suspicious project folder...");
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        Enumeration en = scanRoot.getChildren(false);
        while (en.hasMoreElements()) {
            FileObject fo = (FileObject) en.nextElement();
            if (fo.isFolder()) {
                List nested = scanForProjectsRecursively(fo, deep -1);  // RECURSION
                projects.addAll(nested);
            }
        }
        return projects;
    }

    private static Action findAction (String key) {
        FileObject fo =
            Repository.getDefault().getDefaultFileSystem().findResource(key);

        if (fo != null && fo.isValid()) {
            try {
                DataObject dob = DataObject.find (fo);
                InstanceCookie ic =
                    (InstanceCookie) dob.getCookie(InstanceCookie.class);

                if (ic != null) {
                    Object instance = ic.instanceCreate();
                    if (instance instanceof Action) {
                        Action a = (Action) instance;
                        // NewProject action reads the properties PRESELECT_CATEGORY and PRESELECT_TEMPLATE
                        a.putValue ("PRESELECT_CATEGORY", "General");
                        a.putValue ("PRESELECT_TEMPLATE", null);
                        return a;
                    }
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                return null;
            }
        }
        return null;
    }


    private static boolean performAction (Action a) {
        if (a == null) {
            return false;
        }
        ActionEvent ae = new ActionEvent(ProjectUtilities.class, ActionEvent.ACTION_PERFORMED, "command");
        try {
            a.actionPerformed(ae);
            return true;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            return false;
        }
    }

}
