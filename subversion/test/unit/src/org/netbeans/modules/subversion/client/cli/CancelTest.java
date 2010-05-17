/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.cli;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author tomas
 */
public class CancelTest extends AbstractCLITest {

    public CancelTest(String testName) throws Exception {
        super(testName);
    }
            
    public void testCancel() throws Exception {                                                
        final ISVNClientAdapter c = getNbClient();        
        Blocker blocker = new Blocker("cli: process created");
        
        Logger.getLogger("").addHandler(blocker);
        RequestProcessor rp = new RequestProcessor("clitest");
        rp.post(new Runnable() {
            public void run() {
                try {
                    c.getInfo(getRepoUrl());
                } catch (SVNClientException ex) {
                    System.out.println("");
                }
            }
        }); 
        Thread.sleep(1000);
        c.cancelOperation();
        assertTrue(blocker.destroyed);
    }            
    
    private class Blocker extends Handler {
        private final String msg;
        private boolean destroyed = false;
        public Blocker(String msg) {
            this.msg = msg;
        }
        @Override
        public void publish(LogRecord record) {
            if(record.getMessage().indexOf(msg) > -1) {
                while(true) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            } else if(record.getMessage().indexOf("cli: Process destroyed") > -1) {
                destroyed = true;
            }
        }
        @Override
        public void flush() { }
        @Override
        public void close() throws SecurityException { }
    }
    
}
