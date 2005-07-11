/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.project.uiapi.ActionsFactory;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.NodeAction;

/** Factory for all kinds of actions used in projectui and
 *projectuiapi.
 */
public class Actions implements ActionsFactory {
    
    //private static final Actions INSTANCE = new Actions();  
    
    public Actions() {};
    
    
    // Implementation of ActionFactory -----------------------------------------
    
    private static Action SET_AS_MAIN_PROJECT;
    private static Action CUSTOMIZE_PROJECT;
    private static Action OPEN_SUBPROJECTS;
    private static Action CLOSE_PROJECT;
    private static Action NEW_FILE;
    private static Action DELETE_PROJECT;
            
    public synchronized Action setAsMainProjectAction() {
        if ( SET_AS_MAIN_PROJECT == null ) {
            SET_AS_MAIN_PROJECT = new SetMainProject();
        }
        return SET_AS_MAIN_PROJECT;
    }
    
    public synchronized Action customizeProjectAction() {
        if ( CUSTOMIZE_PROJECT == null ) {
            CUSTOMIZE_PROJECT = new CustomizeProject();
        }
        return CUSTOMIZE_PROJECT;
    }
    
    public synchronized Action openSubprojectsAction() {
        if ( OPEN_SUBPROJECTS == null ) {
            OPEN_SUBPROJECTS = new OpenSubprojects();
        }
        return OPEN_SUBPROJECTS;
    }
    
    public synchronized Action closeProjectAction() {
        if ( CLOSE_PROJECT == null ) {
            CLOSE_PROJECT = new CloseProject();
        }
        return CLOSE_PROJECT;        
    }
    
    public synchronized Action newFileAction() {
        if ( NEW_FILE == null ) {
            NEW_FILE = new NewFile.WithSubMenu();
        }
        return NEW_FILE;
    }    
    
    public Action deleteProjectAction() {
        return deleteProject();
    }

    public synchronized Action newProjectAction() {
        return new NewProject();
    }
    
    public Action projectCommandAction(String command, String namePattern, Icon icon ) {
        return new ProjectAction( command, namePattern, icon, null );
    }
    
    public Action projectSensitiveAction( ProjectActionPerformer performer, String namePattern, Icon icon ) {
        return new ProjectAction( performer, namePattern, icon, null );
    }
    
    public Action mainProjectCommandAction(String command, String name, Icon icon) {
        return new MainProjectAction( command, name, icon );
    }
    
    public Action mainProjectSensitiveAction(ProjectActionPerformer performer, String name, Icon icon) {
        return new MainProjectAction( performer, name, icon );
    }

    
    public Action fileCommandAction(String command, String name, Icon icon) {
        return new FileCommandAction( command, name, icon, null );
    }
    
    // Project specific actions ------------------------------------------------
    
    public static Action javadocProject() {
        return new ProjectAction (
            "javadoc", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_JavadocProjectAction_Name" ), // NOI18N
            null, 
            null ); 
    }
    
    public static Action testProject() {        
        Action a = new ProjectAction (
            "test", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_TestProjectAction_Name" ), // NOI18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/testProject.png" ) ), //NOI18N
            null ); 
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/testProject.png"); //NOI18N
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        return a;
    }
        
    
    public static Action buildProject() {
        Action a = new ProjectAction (
            ActionProvider.COMMAND_BUILD, 
            NbBundle.getMessage(Actions.class, "LBL_BuildProjectAction_Name" ), // NO18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/buildCurrentProject.gif" ) ), //NOI18N
            null );  
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/buildCurrentProject.gif"); //NOI18N
        return a;
    }
    
    public static Action rebuildProject() {
        Action a = new ProjectAction(
            ActionProvider.COMMAND_REBUILD,
            NbBundle.getMessage(Actions.class, "LBL_RebuildProjectAction_Name"),  // NOI18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/rebuildCurrentProject.gif" ) ), //NOI18N
            null ); 
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/rebuildCurrentProject.gif"); //NOI18N
        return a;
    }
        
    public static Action runProject() {
        Action a = new ProjectAction(
            ActionProvider.COMMAND_RUN, 
            NbBundle.getMessage(Actions.class, "LBL_RunProjectAction_Name"), // NO18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/runCurrentProject.gif" ) ), //NOI18N
            null ); 
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/runCurrentProject.gif"); //NOI18N
        return a;
    }
    
    public static synchronized Action deleteProject() {
        if (DELETE_PROJECT == null) {
            DELETE_PROJECT = new ProjectAction(
                ActionProvider.COMMAND_RUN, 
                NbBundle.getMessage(Actions.class, "LBL_DeleteProjectAction_Name"), // NO18N
                null,
                null );
        }
        
        return DELETE_PROJECT;
    }
    
    // 1-off actions -----------------------------------------------------------
    
    public static Action compileSingle() {
        Action a = new FileCommandAction (
            "compile.single", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_CompileSingleAction_Name" ),// NOI18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/compileSingle.png" ) ), //NOI18N
            null ); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/compileSingle.png"); //NOI18N
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        return a;
    }
    
    public static Action runSingle() {
        Action a = new FileCommandAction (
            "run.single", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_RunSingleAction_Name"), // NOI18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/runSingle.png" ) ), //NOI18N
            null);
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/runSingle.png"); //NOI18N
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        return a;
    }
    
    public static Action debugSingle() {
        Action a = new FileCommandAction (
            "debug.single", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_DebugSingleAction_Name"), // NOI18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/debugSingle.png" ) ), //NOI18N
            null);
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/debugSingle.png"); //NOI18N
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        return a;
    }
    
    public static Action testSingle() {
        Action a = new FileCommandAction (
            "test.single", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_TestSingleAction_Name" ),// NOI18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/testSingle.png" ) ), //NOI18N
            null ); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/testSingle.png"); //NOI18N
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        return a;
    }
    
    public static Action debugTestSingle()  {
        Action a = new FileCommandAction (
            "debug.test.single", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_DebugTestSingleAction_Name" ),// NOI18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/debugTestSingle.png" ) ), //NOI18N
            null ); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/debugTestSingle.png"); //NOI18N
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        return a;
    }
        
    // Main Project actions ----------------------------------------------------
    
    
    public static Action buildMainProject() {
        Action a = new MainProjectAction (
            ActionProvider.COMMAND_BUILD, 
            NbBundle.getMessage(Actions.class, "LBL_BuildMainProjectAction_Name" ),
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/buildProject.gif" ) ) );  //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/buildProject.gif"); //NOI18N
        return a;
    }
    
    public static Action rebuildMainProject() {
        Action a = new MainProjectAction(
            ActionProvider.COMMAND_REBUILD,
            NbBundle.getMessage(Actions.class, "LBL_RebuildMainProjectAction_Name"),  // NOI18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/rebuildProject.gif" ) ) ); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/rebuildProject.gif"); //NOI18N
        return a;
    }
        
    public static Action runMainProject() {
        Action a = new MainProjectAction(
            ActionProvider.COMMAND_RUN,
            NbBundle.getMessage(Actions.class, "LBL_RunMainProjectAction_Name"), // NO18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/runProject.gif" ) ) ); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/runProject.gif"); //NOI18N
        return a;
    }
    
    public static Action debugMainProject() {
        Action a = new MainProjectAction(
            ActionProvider.COMMAND_DEBUG, 
            NbBundle.getMessage(Actions.class, "LBL_DebugMainProjectAction_Name" ), // NOI18N
            new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/debugProject.gif" ) ) ); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/debugProject.gif"); //NOI18N
        return a;
    }

}
