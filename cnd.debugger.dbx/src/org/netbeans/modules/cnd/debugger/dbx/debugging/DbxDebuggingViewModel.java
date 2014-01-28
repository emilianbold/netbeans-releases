/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.debugger.dbx.debugging;

import org.netbeans.modules.cnd.debugger.common2.debugger.debugging.DebuggingViewModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.TreeModel;

/**
 *
 * @author Nikolay Koldunov
 */
@DebuggerServiceRegistration(path="netbeans-DbxSession/DebuggingView",
                             types={TreeModel.class/*, AsynchronousModelFilter.class*/},
                             position=10000)
public class DbxDebuggingViewModel extends DebuggingViewModel {
    public DbxDebuggingViewModel(ContextProvider lookupProvider) {
        super(lookupProvider);
    }
}
