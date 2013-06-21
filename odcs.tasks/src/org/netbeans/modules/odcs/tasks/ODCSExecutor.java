/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.tasks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.mylyn.util.PerformQueryCommand;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class ODCSExecutor {
    private final ODCSRepository repository;
    
    public ODCSExecutor (ODCSRepository repository) {
        this.repository = repository;
    }
    
    public void execute(BugtrackingCommand cmd) {
        execute(cmd, true, true);
    }
    
    public void execute(BugtrackingCommand cmd, boolean ensureCredentials, boolean handleExceptions) {
        cmd.setFailed(true);
        try {
            if (ensureCredentials) {
                repository.ensureCredentials();
            }
            cmd.execute();
            if(cmd instanceof PerformQueryCommand) {
                handleStatus((PerformQueryCommand) cmd, handleExceptions);
            }
            cmd.setFailed(false);
            cmd.setErrorMessage(null);
        } catch (CoreException ex) {
            notifyError(ex);
        } catch (MalformedURLException ex) {
            ODCS.LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ODCS.LOG.log(Level.SEVERE, null, ex);
        } catch (RuntimeException re) {
            Throwable t = re.getCause();
            if(t instanceof InterruptedException || !handleExceptions) {
                ODCS.LOG.log(Level.FINE, null, t);
            } else {
                ODCS.LOG.log(Level.SEVERE, null, re);
            }
        } 
    }
    
    private static void notifyError(CoreException ce) {
        String msg = getMessage(ce);
        IStatus status = ce.getStatus();
        if (msg == null && status instanceof RepositoryStatus) {
            RepositoryStatus rs = (RepositoryStatus) status;
            String html = rs.getHtmlMessage();
            if(html != null) {
                msg = html;
                assertHtmlMsg(html); // any reason to expect this ???
            }
        }
        ODCS.LOG.log(Level.INFO, null, ce);
        notifyErrorMessage(msg);
    }
    
    private static String getMessage(CoreException ce) {
        String msg = ce.getMessage();
        if(msg != null && !msg.trim().equals("")) {                             // NOI18N
            return msg;
        }
        IStatus status = ce.getStatus();
        msg = status != null ? status.getMessage() : null;
        return msg != null ? msg.trim() : null;
    }

    @NbBundle.Messages({"# {0} - the returned error message", "MSG_Error_Warning=The following error was returned:\n\n{0}"})  // NOI18N
    private boolean handleStatus(PerformQueryCommand cmd, boolean handleExceptions) throws CoreException {
        IStatus status = cmd.getStatus();
        if(status == null || status.isOK()) {
            return false;
        }
        ODCS.LOG.log(Level.FINE, "command {0} returned status : {1}", new Object[] {cmd, status.getMessage()}); // NOI18N

        Throwable t = status.getException();
        if (t instanceof CoreException) {
            throw (CoreException) status.getException();
        } else {
            ODCS.LOG.log(Level.WARNING, null, t);
        }

        boolean isHtml = false;
        String errMsg = null;
        if(status instanceof RepositoryStatus) {
            RepositoryStatus rstatus = (RepositoryStatus) status;
            errMsg = rstatus.getHtmlMessage();
            isHtml = errMsg != null;
        }
        if(errMsg == null) {
            errMsg = status.getMessage();
        }
        cmd.setErrorMessage(errMsg);
        cmd.setFailed(true);

        if(!handleExceptions) {
            return true;
        }

        if(isHtml) {
            assertHtmlMsg(errMsg); // any reason to expect this ???
        } 
        
        notifyErrorMessage(Bundle.MSG_Error_Warning(errMsg)); 
        return true;
    }    

    @NbBundle.Messages({"LBL_Error=Error"})  // NOI18N
    static void notifyErrorMessage(String msg) {
        if("true".equals(System.getProperty("netbeans.t9y.throwOnClientError", "false"))) { // NOI18N
            ODCS.LOG.info(msg);
            throw new AssertionError(msg);
        }
        NotifyDescriptor nd =
                new NotifyDescriptor(
                    msg,
                    Bundle.LBL_Error(),    
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
    }    
    
    private static void assertHtmlMsg(String msg) {
        assert false : "received html error msg: " + msg; // NOI18N
        ODCS.LOG.log(Level.WARNING, "received html error msg:{0}", msg); // NOI18N
    }    
}
