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
