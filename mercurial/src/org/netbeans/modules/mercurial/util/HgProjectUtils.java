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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial.util;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.ContextAwareAction;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.netbeans.modules.mercurial.Mercurial;
import java.util.logging.Level;


public class HgProjectUtils {
    private static final String ProjectTab_ID_LOGICAL = "projectTabLogical_tc"; // NOI18N    
    
    public static void renameProject(Project p, Object caller) {
        ContextAwareAction action = (ContextAwareAction) CommonProjectActions.renameProjectAction();
        Lookup ctx = Lookups.singleton(p);
        Action ctxAction = action.createContextAwareInstance(ctx);
        ctxAction.actionPerformed(new ActionEvent(caller, 0, "")); // NOI18N
    }

    public static void openProject(Project p, Object caller) {
        Project[] projects = new Project[] {p};
        OpenProjects.getDefault().open(projects, false);
        OpenProjects.getDefault().setMainProject(p);
        
        // set as main project and expand
/*        ContextAwareAction action = (ContextAwareAction) CommonProjectActions.setAsMainProjectAction();
        Lookup ctx = Lookups.singleton(p);
        Action ctxAction = action.createContextAwareInstance(ctx);
        ctxAction.actionPerformed(new ActionEvent(caller, 0, "")); // NOI18N
*/
        selectAndExpandProject(p);
    }
    
    public static void selectAndExpandProject( final Project p ) {
        
        // invoke later to select the being opened project if the focus is outside ProjectTab
        SwingUtilities.invokeLater(new Runnable() {
            
            final ExplorerManager.Provider ptLogial = findDefault(ProjectTab_ID_LOGICAL);
            
            public void run() {
                Node root = ptLogial.getExplorerManager().getRootContext();
                // Node projNode = root.getChildren ().findChild( p.getProjectDirectory().getName () );
                Node projNode = root.getChildren().findChild( ProjectUtils.getInformation( p ).getName() );
                if ( projNode != null ) {
                    try {
                        ptLogial.getExplorerManager().setSelectedNodes( new Node[] { projNode } );
                    } catch (Exception ignore) {
                        // may ignore it
                    }
                }
            }
        });
    }
    
    public static String getProjectName( final File root ) {
        if(root == null) return null;
        final ProjectManager projectManager = ProjectManager.getDefault();
        FileObject rootFileObj = FileUtil.toFileObject(FileUtil.normalizeFile(root));
        String res = null;
        
        if (projectManager.isProject(rootFileObj)){
            try         {
                Project prj = projectManager.findProject(rootFileObj);
                
                res = getProjectName(prj);
            } catch (Exception ex) {
                Mercurial.LOG.log(Level.FINE, "getProjectName() file: {0} {1}", new Object[] {rootFileObj.getPath(), ex.toString()}); // NOI18N
            }finally{
                return res;
            } 
        }else{
            return res;
        }
    }

    public static String getProjectName( final Project p ) {
        
        final ExplorerManager.Provider ptLogial = findDefault(ProjectTab_ID_LOGICAL);
        
        return (p == null) ? null: ProjectUtils.getInformation( p ).getName();
    }
      
    private static synchronized ExplorerManager.Provider findDefault( String tcID ) {
        TopComponent tc = WindowManager.getDefault().findTopComponent( tcID );
        return (ExplorerManager.Provider) tc;
    }    
    
    // Should not be creating an instance of this class
    private HgProjectUtils() {
    }
    
}
