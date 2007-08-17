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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.proxy;

/*
 * GdbLogger.java
 *
 * @author Nik Molchanov
 *
 * Originally this class was in org.netbeans.modules.cnd.debugger.gdb package.
 * Later a new "proxy" package was created and this class was moved, that's how
 * it lost its history. To view the history look at the previous location.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

/**
 * Class GdbLogger is used to log all incoming and outgoing messages
 */
public class GdbLogger {
    
    private GdbConsoleWindow gdbConsoleWindow = null;
    private FileWriter logFile;
    private Logger log = Logger.getLogger("gdb.gdbproxy.logger"); // NOI18N
    
    /** Creates a new instance of GdbLogger */
    public GdbLogger(GdbDebugger debugger, GdbProxy gdbProxy) {
        File tmpfile;
        try {
            tmpfile = File.createTempFile("gdb-cmds", ".log"); // NOI18N
            tmpfile.deleteOnExit();
            logFile = new FileWriter(tmpfile);
        } catch (IOException ex) {
            logFile = null;
        }
        
        if (Boolean.getBoolean("gdb.console.window")) { // NOI18N
            gdbConsoleWindow = GdbConsoleWindow.getInstance(debugger, gdbProxy);
            gdbConsoleWindow.openConsole();
        }
    }
    
    /**
     * Sends message to the debugger log. If console property is set also send it
     * to the console.
     *
     * @param message - a message from the debugger
     */
    public void logMessage(String message) {
        if (message != null && message.length() > 0) {
            if (!message.endsWith("\n")) { // NOI18N
                message = message + '\n';
            }
            if (logFile != null) {
                try {
                    logFile.write(message);
                    logFile.flush();
                } catch (IOException ioex) {
                }
            }
            if (gdbConsoleWindow != null) {
                gdbConsoleWindow.add(message);
            }
        }
    }
}
