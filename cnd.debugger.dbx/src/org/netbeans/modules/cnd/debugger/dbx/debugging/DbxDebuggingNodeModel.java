/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.debugger.dbx.debugging;

import org.netbeans.modules.cnd.debugger.common2.debugger.debugging.DebuggingNodeModel;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.NodeModel;

/**
 *
 * @author Nikolay Koldunov
 */
@DebuggerServiceRegistration(path = "netbeans-DbxSession/DebuggingView",
            types = NodeModel.class,
            position = 400)
public class DbxDebuggingNodeModel extends DebuggingNodeModel {
    public DbxDebuggingNodeModel() {
    }
}
