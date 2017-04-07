/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.debugger.dbx;

import javax.swing.Action;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativePinWatchValueProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;

/**
 *
 * @author Nikolay Koldunov
 */
@DebuggerServiceRegistration(path = "netbeans-DbxDebuggerEngine", types = PinWatchUISupport.ValueProvider.class)
public class DbxPinWatchValueProvider extends NativePinWatchValueProvider {

    public DbxPinWatchValueProvider(ContextProvider lookupProvider) {
        super(lookupProvider);
    }

    @Override
    public Action[] getHeadActions(Watch watch) {
        return new Action[]{new ExpandAction(getDebugger(), watch)};
    }
    
    
}
