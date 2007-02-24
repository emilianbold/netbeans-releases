/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.uml.project;

import java.io.IOException;
import org.netbeans.modules.uml.project.ui.nodes.UMLPhysicalViewProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

public class UMLActionProvider implements ActionProvider
{
    // Commands available from J2SE project
    private static final String[] supportedActions =
    {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DELETE
    };
    
    private UMLProject mProject = null;
    private UMLProjectHelper mHelper = null;
    
    public UMLActionProvider(UMLProject project, UMLProjectHelper helper)
    {
        mProject = project;
        mHelper = helper;
    }
    
    public String[] getSupportedActions()
    {
        return supportedActions;
    }
    
    public void invokeAction(String command, Lookup context)
        throws IllegalArgumentException
    {
        if (command.equals(COMMAND_DELETE))
        {
            UMLPhysicalViewProvider provider = (UMLPhysicalViewProvider)mProject.
                getLookup().lookup(UMLPhysicalViewProvider.class);
            
            try
            {
                provider.createLogicalView().destroy();
            }
            
            catch (IOException e)
            {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException
    {
        return true;
    }
}