/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.debugger.dbx.debugging;

import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugging.DebuggingNodeActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.NodeActionsProvider;

/**
 *
 * @author Nikolay Koldunov
 */
@DebuggerServiceRegistration(path="netbeans-DbxSession/DebuggingView",
                             types=NodeActionsProvider.class,
                             position=700)
public class DbxDebuggingNodeActionsProvider extends DebuggingNodeActionsProvider{
    public DbxDebuggingNodeActionsProvider(ContextProvider lookupProvider) {
        super(lookupProvider);
    }

    @Override
    public void performDefaultAction(final Object node)/* throws UnknownTypeException */{
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                makeCurrent(node);
            }
        });
    }
}
