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

import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.project.uiapi.ActionsFactory;
import org.netbeans.spi.project.ActionProvider;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
            
    public synchronized Action setAsMainProjectAction() {
        return org.openide.util.actions.SystemAction.get(SetAsMainProject.class); // XXX
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
            NEW_FILE = new NewFile();
        }
        return NEW_FILE;
    }    
    
    public Action projectCommandAction(String command, boolean multiselection, String namePattern, Icon icon ) {
        return new ProjectCommandAction( command, namePattern, icon, null );
    }
    
    // Project specific actions ------------------------------------------------
    
    public static Action javadocProject() {
        return new ProjectCommandAction (
            "javadoc", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_JavadocProjectAction_Name" ), // NI18N
            "org/netbeans/modules/project/ui/resources/javadocProject.gif", //NOI18N
            null ); 
    }
    
    public static Action testProject() {        
        return new ProjectCommandAction (
            "test", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_TestProjectAction_Name" ), // NI18N
            "org/netbeans/modules/project/ui/resources/testProject.gif", //NOI18N
            null ); 
    }
    
    // 1-off actions -----------------------------------------------------------
    
    public static Action compileSingle() {
        return new FileCommandAction (
            "compile.single", 
            NbBundle.getMessage(Actions.class, "LBL_CompileSingleAction_Name" ),// NI18N
            "org/netbeans/modules/project/ui/resources/compileSingle.gif",
            null ); //NOI18N
    }
    
    public static Action testSingle() {
        return new FileCommandAction (
            "test.single", 
            NbBundle.getMessage(Actions.class, "LBL_TestSingleAction_Name" ),// NI18N
            "org/netbeans/modules/project/ui/resources/testSingle.gif",
            null ); //NOI18N
    }
    
    public static Action debugTestSingle()  {
        return new FileCommandAction (
            "debug.test.single", 
            NbBundle.getMessage(Actions.class, "LBL_DebugTestSingleAction_Name" ),// NI18N
            "org/netbeans/modules/project/ui/resources/testDebugSingle.gif",
            null ); //NOI18N
    }
    
    public static Action debugFix() {
        return new FileCommandAction (
            "debug.fix",
            NbBundle.getMessage(Actions.class, "LBL_DebugFixAction_Name" ), // NI18N
            "org/netbeans/modules/project/ui/resources/debugFix.gif",
            null ); //NOI18N
    }
    
    // Main Project actions ----------------------------------------------------
    
    
    public static Action buildMainProject() {
        return new MainProjectCommandAction (
            ActionProvider.COMMAND_BUILD, 
            NbBundle.getMessage(Actions.class, "LBL_BuildMainProjectAction_Name" ),
            "org/netbeans/modules/project/ui/resources/buildProject.gif" ); 
    }
    
    public static Action rebuildMainProject() {
        return new MainProjectCommandAction(
            ActionProvider.COMMAND_REBUILD,
            NbBundle.getMessage(Actions.class, "LBL_RebuildMainProjectAction_Name"),  // NI18N 
            "org/netbeans/modules/project/ui/resources/rebuildProject.gif" ); //NOI18N                
    }
        
    public static Action runMainProject() {
        return new MainProjectCommandAction(
            "run", // XXX Define standard 
            NbBundle.getMessage(Actions.class, "LBL_RunMainProjectAction_Name"), // NO18N
            "org/netbeans/modules/project/ui/resources/runProject.gif" ); //NOI18N
    }
    
    public static Action debugMainProject() {
        return new MainProjectCommandAction(
            "debug", // XXX Define standard
            NbBundle.getMessage(Actions.class, "LBL_DebugMainProjectAction_Name" ), // NI18N
            "org/netbeans/modules/project/ui/resources/debugProject.gif" ) ; //NOI18N
    }
        

    // Unimplemented actions ---------------------------------------------------
    
    
    public static Action stopBuilding() {
        return new UnimplementedAction (        
            NbBundle.getMessage(Actions.class, "LBL_StopBuildingAction_Name"), // NI18N
            "org/netbeans/modules/project/ui/resources/stopBuild.gif" ); // NOI18N
    }
    
    public static Action runSingle() {
        return new UnimplementedAction (        
            NbBundle.getMessage(Actions.class, "LBL_RunSingleAction_Name"), // NI18N
            "org/netbeans/modules/project/ui/resources/runSingle.gif" ); // NOI18N
        
    }
    
    public static Action importProject() {
        
        return new UnimplementedAction (
            NbBundle.getMessage(Actions.class, "LBL_ImportProjectAction_Name"), // NI18N
            "org/netbeans/modules/project/ui/resources/openProject.gif" ); // NOI18N
    }
    
    // Private extra actions
    
    /** New file action which implements the node action - good for the 
     * Hack class
     */
    public static class SystemNewFile extends NodeAction {
        
        public SystemNewFile() {}
            
        public String getName() {
            return NbBundle.getMessage( Actions.class, "LBL_NewFileAction_Name"); // NI18N
        }


        public String iconResource() {
            return "org/netbeans/modules/project/ui/resources/newFile.gif"; //NOI18N
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable( Node[] activatedNodes ) {
            if ( activatedNodes.length != 1 ) {
                return false;
            }
            
            Action subst = getSubstitute( activatedNodes[0].getLookup() );
            return subst.isEnabled();
        }

        protected void performAction( Node[] activatedNodes) {
            if ( activatedNodes.length != 1 ) {
                return;
            }
            
            Action subst = getSubstitute( activatedNodes[0].getLookup() );
            subst.actionPerformed( new java.awt.event.ActionEvent( activatedNodes[0].getLookup(), 0, null ) );            
        }
        
        private Action getSubstitute( Lookup actionContext ) {
             return ((ContextAwareAction)org.netbeans.spi.project.ui.support.LogicalViews.newFileAction()).createContextAwareInstance( actionContext );
        }

    }
    
    
    
}
