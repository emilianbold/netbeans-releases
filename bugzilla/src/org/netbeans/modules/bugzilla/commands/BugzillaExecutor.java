/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.commands;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaStatus;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Executes commands against one bugzilla Repository handles errors
 * 
 * @author Tomas Stupka
 */
public class BugzillaExecutor {

    private static final String HTTP_ERROR_NOT_FOUND         = "http error: not found";         // NOI18N
    private static final String INVALID_USERNAME_OR_PASSWORD = "invalid username or password";  // NOI18N

    private final BugzillaRepository repository;

    public BugzillaExecutor(BugzillaRepository repository) {
        this.repository = repository;
    }

    public void execute(BugzillaCommand cmd) {
        execute(cmd, true);
    }

    public void execute(BugzillaCommand cmd, boolean handleExceptions) {
        try {
            cmd.execute();

            cmd.setFailed(false);
            cmd.setErrorMessage(null);

        } catch (CoreException ce) {
            ExceptionHandler handler = ExceptionHandler.createHandler(ce, this);
            if(handler != null) {

                String msg = handler.getMessage();

                cmd.setFailed(true);
                cmd.setErrorMessage(msg);

                if(handleExceptions) {
                    if(handler.handle()) {
                        // execute again
                        execute(cmd);
                    } else {
                        // notify user cancel
                        notifyErrorMessage(NbBundle.getMessage(BugzillaExecutor.class, "MSG_ActionCanceledByUser")); // NOI18N
                    }
                }
            }

            return;
                
        } catch(MalformedURLException me) {
            cmd.setFailed(true); // should not happen
            cmd.setErrorMessage(me.getMessage());
            Bugzilla.LOG.log(Level.SEVERE, null, me);
        } catch(IOException ioe) {
            cmd.setFailed(true);
            cmd.setErrorMessage(ioe.getMessage());

            if(!handleExceptions) {
                return;
            }

            handleIOException(ioe);
        } 
    }

    public boolean handleIOException(IOException io) {
        Bugzilla.LOG.log(Level.SEVERE, null, io); 
        return true;
    }

    private static void notifyErrorMessage(String msg) {
        NotifyDescriptor nd =
                new NotifyDescriptor(
                    msg, 
                    NbBundle.getMessage(BugzillaExecutor.class, "LBLError"),    // NOI18N
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
    }

    private static abstract class ExceptionHandler {

        protected String errroMsg;
        protected CoreException ce;
        protected BugzillaExecutor executor;

        protected ExceptionHandler(CoreException ce, String msg, BugzillaExecutor executor) {
            this.errroMsg = msg;
            this.ce = ce;
            this.executor = executor;
        }

        static ExceptionHandler createHandler(CoreException ce, BugzillaExecutor executor) {
            String errormsg = getAuthenticateError(ce);
            if(errormsg != null && INVALID_USERNAME_OR_PASSWORD.equals(errormsg.trim().toLowerCase())) {
                return new AuthenticationHandler(
                        ce,
                        errormsg,
                        executor);
            }
            errormsg = getNotFoundError(ce);
            if(errormsg != null) {
                return new NotFoundHandler(
                        ce, 
                        errormsg,
                        executor);
            }

            return new DefaultHandler(ce, null, null, executor);
        }

        abstract boolean handle();

        private static String getAuthenticateError(CoreException ce) {
            String msg = getMessage(ce);
            if(msg != null && INVALID_USERNAME_OR_PASSWORD.equals(msg.trim().toLowerCase())) {
                return "Invalid Username or Password"; // XXX bundle me
            }
            return null;
        }

        private static String getNotFoundError(CoreException ce) {
            IStatus status = ce.getStatus();
            Throwable t = status.getException();
            if(t instanceof UnknownHostException) {
                return "Host not found";
            }
            String msg = getMessage(ce);
            if(msg != null && HTTP_ERROR_NOT_FOUND.equals(msg.trim().toLowerCase())) {
                return "Host not found";
            }
            return null;
        }


        static String getMessage(CoreException ce) {
            String msg = ce.getMessage();
            if(msg != null && !msg.trim().equals("")) {                             // NOI18N
                return msg;
            }
            IStatus status = ce.getStatus();
            msg = status != null ? status.getMessage() : null;
            return msg != null ? msg.trim() : null;
        }

        String getMessage() {
            return errroMsg;
        }

        private static void notifyError(CoreException ce) {
            IStatus status = ce.getStatus();
            if (status instanceof RepositoryStatus) {
                RepositoryStatus rs = (RepositoryStatus) status;
                String html = rs.getHtmlMessage();
                if(html != null && !html.trim().equals("")) {                       // NOI18N
                    final HtmlPanel p = new HtmlPanel();
                    p.setHtml(html);
                    NotifyDescriptor descriptor = new NotifyDescriptor (
                        p,
                        NbBundle.getMessage(BugzillaExecutor.class, "MSG_RemoteResponse"),
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.INFORMATION_MESSAGE,
                        new Object[] {NotifyDescriptor.OK_OPTION},
                        NotifyDescriptor.OK_OPTION);
                    DialogDisplayer.getDefault().notify(descriptor);
    //                 XXX show in browser ?
                    return;
                }
            }
            String msg = getMessage(ce);
            notifyErrorMessage(msg);
        }

        private static class AuthenticationHandler extends ExceptionHandler {
            public AuthenticationHandler(CoreException ce, String msg, BugzillaExecutor executor) {
                super(ce, msg, executor);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                return BugtrackingUtil.editRepository(executor.repository, errroMsg);
            }
        }
        private static class NotFoundHandler extends ExceptionHandler {
            public NotFoundHandler(CoreException ce, String msg, BugzillaExecutor executor) {
                super(ce, msg, executor);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                return BugtrackingUtil.editRepository(executor.repository, errroMsg);
            }
        }
        private static class DefaultHandler extends ExceptionHandler {
            public DefaultHandler(CoreException ce, String msg, BugzillaCommand cmd, BugzillaExecutor executor) {
                super(ce, msg, executor);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                notifyError(ce);
                return false;
            }
        }
    }
}

