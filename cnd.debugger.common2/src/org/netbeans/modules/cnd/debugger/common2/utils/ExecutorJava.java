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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

import org.openide.ErrorManager;

import org.netbeans.modules.cnd.debugger.common2.debugger.io.TermComponent;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Platform;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import java.util.Map;

/* package */ class ExecutorJava extends Executor {
    private Process engineProc;

    public ExecutorJava(String name, Host host) {
	super(name, host);
    }

    public String slaveName() {
	return null;
    }

    public boolean isAlive() {
	if (isRemote()) {
	    return true;
	} else {
	    try {
		engineProc.exitValue();
	    } catch (IllegalThreadStateException x) {
		return true;
	    }
	    return false;
	}
    }

    public void terminate() throws IOException {
	if (isRemote()) {
	} else {
	    engineProc.destroy();	// On unix this sends a SIGTERM
	}
    }

    /*
     * Interrupt an arbitrary process with SIGINT
     */
    public void interrupt(int pid) throws IOException {
	throw new IOException("NOT IMPLEMENTED");	// NOI18N
    }

    public void interruptGroup() throws IOException {
	if (isRemote()) {
	} else {
	    // No equivalent on Windows or in Java platform
	    // LATER engineProc.killgrp(2);        // 2 == SIGINT
	}
    }

    public void sigqueue(int sig, int data) throws IOException {
	if (isRemote()) {
	} else {
	    // No equivalent on Windows or in Java platform
	    // LATER engineProc.sigqueue(sig, data);
	}
    }

    public synchronized int startShellCmd(String engine_argv[]) {
	throw new UnsupportedOperationException();
    }

    public String getCmdOutput() {
	throw new UnsupportedOperationException();
    }

    public List<String> getCmdOutputLines() {
	throw new UnsupportedOperationException();
    }

    public void runShellCmd(String cmd_argv[]) {
	throw new UnsupportedOperationException();
    }

    public synchronized int startEngine(String enginePath,
					String engine_argv[], Map<String, String> additionalEnv,
			                TermComponent console) {

	String argv[] = new String[engine_argv.length];
	argv[0] = enginePath;
	for (int cx = 1; cx < engine_argv.length; cx++) {
	    argv[cx] = engine_argv[cx];
	}

	// for now ignore additionalEnv ...
	try {
	    engineProc = Runtime.getRuntime().exec(argv);
	} catch (Exception x) {
	    ErrorManager.getDefault().notify(x);
	}

	console.connectIO(engineProc.getOutputStream(),
		          engineProc.getInputStream());

	startMonitor();

	return 1;
    }

    public String getStartError() {
	return null;
    }

    protected int waitForEngine() throws InterruptedException {
	if (engineProc == null)
	    return -1;
	return engineProc.waitFor();
    }

    @Override
    protected void destroyEngine() {
	if (engineProc == null)
	    return;
	super.destroyEngine();
	engineProc.destroy();
    }

    public void cleanup() {
    }

    @Override
    public void reap() {
	throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public synchronized boolean startIO(TermComponent pio) {
	return false;
    }

    public String readlink(long pid) {
	return null;
    }

    public boolean is_64(String p) {
	return false;
    }

    public boolean svc_is64() {
	return false;
    }

    public Platform platform() {
	return null;
    }
	      
    public InputStream getInputStream() {
	return engineProc.getInputStream();
    }

    public OutputStream getOutputStream() {
	return engineProc.getOutputStream();
    }

}
