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

package org.netbeans.modules.java.project;
import javax.swing.Action;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.util.NbBundle;


/**
 * Supplies project-specific file actions (e.g. compile/run/debug) for *.java files.
 * @author Petr Hrebejk
 */
public class JavaProjectModule {
          
    public static Action compile() {
        return FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_COMPILE_SINGLE, 
                       NbBundle.getMessage( JavaProjectModule.class, "LBL_CompileFile_Action" ), // NOI18N
                       null );
    }
            
    public static Action run() {
        return FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_RUN_SINGLE, 
                       NbBundle.getMessage( JavaProjectModule.class, "LBL_RunFile_Action" ), // NOI18N
                       null );
    }
    
    public static Action debug() {
        return FileSensitiveActions.fileCommandAction( 
                       ActionProvider.COMMAND_DEBUG_SINGLE, 
                       NbBundle.getMessage( JavaProjectModule.class, "LBL_DebugFile_Action" ), // NOI18N
                       null );
    }
    
}
