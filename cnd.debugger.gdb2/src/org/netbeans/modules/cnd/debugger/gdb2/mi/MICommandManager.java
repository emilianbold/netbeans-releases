/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.debugger.gdb2.mi;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Manages ...
 * <ul>
 * <li> Queueing of command and sending them to the engine.
 * <li> Receiving of output from engine and parsing and dispatching it.
 */

class MICommandManager {
    private final MICommandInjector injector;
    private int commandToken = 1;
    private final Map<Integer, MICommand> pendingCommands =
            new LinkedHashMap<Integer, MICommand>();

    public MICommandManager(MICommandInjector injector) {
	this.injector = injector;
    } 


    /**
     * Send a command immediately or queue it up if there are
     * pending commands.
     * <br>
     * NOTE: Currently commands are always sent immediately.
     */

    public void send(MICommand cmd) {
	cmd.setManagerData(this, commandToken++);
	pendingCommands.put(cmd.getToken(), cmd);
	injector.inject("" + cmd.getToken() + cmd.command() + "\n"); // NOI18N
	
    }

    /**
     * We're done with this command. 
     * Take it off the pending list send off any queued up commands.
     * <br>
     * NOTE: Currently commands are always sent immediately.
     */

    void finish(MICommand cmd) {
	pendingCommands.remove(cmd.getToken());
	if (Log.MI.finish) {
	    injector.log(String.format("## finished %d\n\r", cmd.getToken())); // NOI18N
	    injector.log(String.format("## outstanding: ")); // NOI18N
	    if (pendingCommands.isEmpty()) {
		injector.log(String.format("none")); // NOI18N
	    } else {
		for (MICommand oc : pendingCommands.values()) {
		    injector.log(String.format(" %d", oc.getToken())); // NOI18N
		}
	    }
	    injector.log(String.format("\n\r")); // NOI18N
	}
    }

    /**
     * To be called from specialization of MIProxy.
     */

    public void dispatch(MIRecord record) {
	int token = record.token();
	MICommand cmd = pendingCommands.get(token);
	if (cmd == null) {
	    injector.log(String.format("No command for record %s\n\r", record)); // NOI18N
	    return;
	}

	record.setCommand(cmd);

	if (record.isError()) {
	    injector.log(record.error() + "\n\r"); // NOI18N
	    finish(cmd);
	    return;
	}

	if (record.type == '^') {
	    if (record.cls.equals("done")) { // NOI18N
		cmd.onDone(record);
	    } else if (record.cls.equals("running")) { // NOI18N
		cmd.onRunning(record);
	    } else if (record.cls.equals("error")) { // NOI18N
		cmd.onError(record);
	    } else if (record.cls.equals("exit")) { // NOI18N
		cmd.onExit(record);
	    } else {
		cmd.onOther(record);
	    }
	} else if (record.type == '*') {
	    if (record.cls.equals("stopped")) { // NOI18N
		cmd.onStopped(record);
	    } else {
		cmd.onOther(record);
	    } 
	} else {
	    cmd.onOther(record);
	} 
    }

    /**
     * Record logStream data into the current pending command.
     */
    void logStream(String data) {
	if (pendingCommands.isEmpty()) {
	    /*
	    System.out.printf("--- logStream dropped\n");
	    */
	    return;
	}
	MICommand oc = pendingCommands.values().iterator().next();
	/* 
	System.out.printf("--- logStream added to %d:\n", oc.getToken());
	System.out.printf("    %s\n", data);
	*/
	oc.recordLogStream(data);
    }

    /**
     * Record logConsole data into the current pending command.
     */
    void logConsole(String data) {
	if (pendingCommands.isEmpty()) {
	    /*
	    System.out.printf("--- logConsole dropped\n");
	    */
	    return;
	}
        // find the first unfinished console command
        MICommand oc = null;
        for (MICommand command : pendingCommands.values()) {
            if (command.isConsoleCommand()) {
                if (oc == null || command.getToken() < oc.getToken()) {
                    oc = command;
                }
            }
        }
	/*
	System.out.printf("--- logConsole added to %d:\n", oc.getToken());
	System.out.printf("    %s\n", data);
	*/
        assert oc != null : "Console output for unknown command: " + data; //NOI18N
        if (oc != null) {
            oc.recordConsoleStream(data);
        }
    }

    /**
     * Echo something on the debugger console.
     */
    void echo(String data) {
	injector.log(data);
    }
}

