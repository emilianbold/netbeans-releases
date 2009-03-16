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
package org.netbeans.modules.php.dbgp.actions;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.packets.BrkpntCommandBuilder;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand;
import org.netbeans.modules.php.dbgp.packets.RunCommand;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.text.Line;


/**
 * @author ads
 *
 */
public class RunToCursorActionProvider extends AbstractActionProvider {

    public RunToCursorActionProvider( ContextProvider contextProvider ) {
        super(contextProvider);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.debugger.ActionsProviderSupport#doAction(java.lang.Object)
     */
    @Override
    public void doAction( Object action )
    {
        SessionId id = getSessionId();
        if ( id ==null ){
            return;
        }
        hideSuspendAnnotations();
        DebugSession session = getSession();
        if ( session == null ){
            return ;
        }
        Line line = Utils.getCurrentLine();
        if (line == null){
            return ;
        }

        BrkpntSetCommand command = BrkpntCommandBuilder.buildLineBreakpoint(
                id, session.getTransactionId(), 
                EditorContextDispatcher.getDefault().getCurrentFile(), 
                line.getLineNumber() );
        command.setTemporary( true );
        session.sendCommandLater(command);
        
        hideSuspendAnnotations();
        RunCommand runCommand = new RunCommand( session.getTransactionId());
        session.sendCommandLater(runCommand);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.debugger.ActionsProvider#getActions()
     */
    @Override
    public Set getActions()
    {
        return Collections.singleton( ActionsManager.ACTION_RUN_TO_CURSOR );
    }

}
