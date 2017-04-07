/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.debugger.dbx.debugging;

import org.netbeans.modules.cnd.debugger.common2.debugger.debugging.NativeDVSupportImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.DebuggingView;

/**
 *
 * @author Nikolay Koldunov
 */
@DebuggingView.DVSupport.Registration(path="netbeans-DbxSession")
public class DbxDVSupportImpl extends NativeDVSupportImpl{
    public DbxDVSupportImpl(ContextProvider lookupProvider) {
        super(lookupProvider);
    }
}
