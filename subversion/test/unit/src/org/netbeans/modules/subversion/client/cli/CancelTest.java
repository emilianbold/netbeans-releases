/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class CancelTest extends AbstractCLITest {

    public CancelTest(String testName) throws Exception {
        super(testName);
    }
    
    public void testCancelBeforeExec() throws Exception {   
        ISVNClientAdapter c = getNbClient();
        cancel(new Blocker("cli: process created", "cli: Process destroyed", c), new CommandRunnable(c));
    }         
    
    public void testCancelDuringOutput() throws Exception {    
        ISVNClientAdapter c = getNbClient();
        cancel(new Blocker("cli: OUTPUT", "cli: Process destroyed", c), new CommandRunnable(getNbClient()));
    }         
    
    public void testCancelDuringError() throws Exception {    
        ISVNClientAdapter c = getNbClient();
        CommandRunnable cmd = new CommandRunnable(c) {
            protected void execCommand() throws Exception {
                c.getInfo(new SVNUrl("file:///hokus/pokus"));
            }
        };
        cancel(new Blocker("cli: ERROR", "cli: Process destroyed", c), cmd);
    }    
    
    public void testCancelBinOutput() throws Exception {    
        final File f = createFile("file");
        commit(f);
        
        ISVNClientAdapter c = getNbClient();
        CommandRunnable cmd = new CommandRunnable(c) {
            protected void execCommand() throws Exception {
                c.getContent(f, SVNRevision.HEAD);
            }
        };
        cancel(new Blocker("cli: ready for binary OUTPUT", "cli: Process destroyed", c), cmd);
    }         
    
    private void cancel(Blocker blocker, CommandRunnable cmd) throws Exception {                                                
        Logger.getLogger("").addHandler(blocker);

        cmd.exec();
        Thread.sleep(1000);
        cmd.task.waitFinished();
        assertTrue(blocker.msgIntercepted);
        assertTrue(blocker.destroyed);       
        if(cmd.th != null) {
            if(!(cmd.th instanceof SVNClientException))  fail(cmd.th.getMessage());  // no exception should be thrown
        }
    }            
    
    private class Blocker extends Handler {
        private final String blockMsg;
        private final String destroyMsg;
        private boolean destroyed = false;
        private boolean msgIntercepted = false;
        private ISVNClientAdapter c;
        private SVNClientException e;
        public Blocker(String blockMsg, String destroyMsg, ISVNClientAdapter c) {
            this.blockMsg = blockMsg;
            this.destroyMsg = destroyMsg;
            this.c = c;
        }
        @Override
        public void publish(LogRecord record) {
            if(record == null) return;
            if(record.getMessage() == null) return;
            if(record.getMessage().indexOf(blockMsg) > -1) {
                msgIntercepted = true;
                while(!destroyed) {
                    try {
                        Thread.sleep(200);
                        c.cancelOperation();
                    } catch (InterruptedException ex) {
                        break;
                    } catch (SVNClientException ex) {
                        e = ex;
                    }
                }
            } else if(record.getMessage().indexOf(destroyMsg) > -1) {
                destroyed = true;
            }
        }
        @Override
        public void flush() { }
        @Override
        public void close() throws SecurityException { }
    }
    
    private class CommandRunnable  {
        private RequestProcessor rp = new RequestProcessor("clitest");
        Throwable th = null;
        protected ISVNClientAdapter c;
        private Task task;

        public CommandRunnable(ISVNClientAdapter c) {
            this.c = c;
        }
        
        public void exec() {
            task = rp.post(new Runnable() {
                public void run() {
                    try {
                        execCommand();
                    } catch (Throwable t) {
                        th = t;
                    }
                }
            });
        }
        protected void execCommand() throws Exception {
            c.getInfo(getRepoUrl());
        }
        void waitFinnishe() {
            task.waitFinished();
        }
    }
            
    
}
