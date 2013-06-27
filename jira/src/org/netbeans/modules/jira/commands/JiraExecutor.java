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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.commands;

import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraServiceUnavailableException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.autoupdate.JiraAutoupdate;
import org.netbeans.modules.jira.kenai.KenaiRepository;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Executes commands against one jira Repository and handles errors
 * 
 * @author Tomas Stupka
 */
public class JiraExecutor {

    private static final String HTTP_ERROR_NOT_FOUND                    = "http error: not found";         // NOI18N
    private static final String INVALID_USERNAME_OR_PASSWORD            = "invalid username or password";  // NOI18N
    private static final String INVALID_USERNAME_OR_PASSWORD_NO_SPACES  = INVALID_USERNAME_OR_PASSWORD.replace(" ", "");  // NOI18N
    private static final String REPOSITORY_LOGIN_FAILURE                = "unable to login to";            // NOI18N
    private static final String COULD_NOT_BE_FOUND                      = "could not be found";            // NOI18N
    private static final String REPOSITORY                              = "repository";                    // NOI18N
    private static final String MIDAIR_COLLISION                        = "mid-air collision occurred while submitting to"; // NOI18N
    private static final String HOST_NOT_FOUND_ERROR                    = "host not found"; //NOI18N

    private final JiraRepository repository;

    private final static Set<String> notifyOnceErrors = new HashSet<>(1);
    
    public JiraExecutor(JiraRepository repository) {
        this.repository = repository;
    }

    public void execute(BugtrackingCommand cmd) {
        execute(cmd, true);
    }

    public void execute(BugtrackingCommand cmd, boolean handleExceptions) {
        execute(cmd, handleExceptions, true, true);
    }

    public void execute(BugtrackingCommand cmd, boolean handleExceptions, boolean ensureConfiguration, boolean checkVersion) {
        execute(cmd, handleExceptions, ensureConfiguration, checkVersion, true);
    }
    
    public void execute(BugtrackingCommand cmd, boolean handleExceptions, boolean ensureConfiguration, boolean checkVersion, boolean ensureCredentials) {
        try {
            try {

                cmd.setFailed(true);

                if (ensureConfiguration) {
                    repository.getConfiguration(); // XXX hack
                }

                if(checkVersion) {
                    checkAutoupdate();
                }

                if(ensureCredentials) {
                    repository.ensureCredentials();
                }
                
                cmd.execute();

                cmd.setFailed(false);
                cmd.setErrorMessage(null);

            } catch (IOException ioe) {
                // XXX
                Throwable cause = ioe.getCause();
                if(cause instanceof JiraException) {
                    JiraException je = (JiraException) cause;
                    Jira.LOG.log(Level.INFO, null, je);
                    throw new WrapperException(je.getMessage(), je);
                }
                throw ioe;
            } catch (CoreException ce) {
                throw new WrapperException(ce.getMessage(), ce);
            } catch(IllegalStateException ise) {
                String msg = ise.getMessage();
                if(msg != null && msg.equals("Connection is not open")) {       // NOI18N
                    Jira.LOG.log(Level.INFO, null, ise);
                    throw new WrapperException(msg, ise);
                } else {
                    throw ise;
                }
            }
        } catch (WrapperException we) {
            ExceptionHandler handler = ExceptionHandler.createHandler(we, this, repository);
            assert handler != null;

            String msg = handler.getMessage();

            cmd.setFailed(true);
            cmd.setErrorMessage(msg);

            if(handleExceptions) {
                if(repository instanceof KenaiRepository && 
                    // jira broken on kenai. it should be enough to notify and log only one time.
                    msg.equals("No JIRA repository found at location. Invalid URL or proxy problem.")) 
                {
                    if(notifyOnceErrors.contains(msg)) {
                        return;
                    } else {
                        notifyOnceErrors.add(msg);
                    }
                }
                
                Jira.LOG.log(Level.INFO, null, we.getCause());
                if(handler.handle()) {
                    // execute again
                    execute(cmd, handleExceptions, ensureConfiguration, checkVersion);
                }
            }
            return;
                
        } catch(MalformedURLException me) {
            cmd.setErrorMessage(me.getMessage());
            Jira.LOG.log(Level.SEVERE, null, me);
        } catch(IOException ioe) {
            cmd.setErrorMessage(ioe.getMessage());

            if(!handleExceptions) {
                Jira.LOG.log(Level.FINE, null, ioe);
                return;
            }

            handleIOException(ioe);
        } catch(RuntimeException re) {
            Throwable t = re.getCause();
            if(t instanceof InterruptedException || !handleExceptions) {
                Jira.LOG.log(Level.FINE, null, t);
            } else {
                Jira.LOG.log(Level.SEVERE, null, re);
            }
        }
    }

    public boolean handleIOException(IOException io) {
        Jira.LOG.log(Level.SEVERE, null, io);
        return true;
    }

    private void checkAutoupdate() {
        try {
            JiraAutoupdate.getInstance().checkAndNotify(repository);
        } catch(Throwable t) {
            Jira.LOG.log(Level.SEVERE, "Exception in JIRA autoupdate check.", t);
        }
    }

    private static abstract class ExceptionHandler {

        protected String erroMsg;
        protected WrapperException ex;
        protected JiraExecutor executor;
        protected JiraRepository repository;

        protected ExceptionHandler(WrapperException ex, String msg, JiraExecutor executor, JiraRepository repository) {
            this.erroMsg = msg == null ? ex.getMessage() : msg;
            this.ex = ex;
            this.executor = executor;
            this.repository = repository;
        }

        static ExceptionHandler createHandler(WrapperException ex, JiraExecutor executor, JiraRepository repository) {
            String errormsg = getLoginError(ex);
            if(errormsg != null) {
                return new LoginHandler(ex, errormsg, executor, repository);
            }
            errormsg = getNotFoundError(ex);
            if(errormsg != null) {
                return new NotFoundHandler(ex, errormsg, executor, repository);
            }
            errormsg = getMidAirColisionError(ex);
            if(errormsg != null) {
                errormsg = MessageFormat.format(errormsg, repository.getDisplayName());
                return new DefaultHandler(ex, errormsg, executor, repository);
            }
            return new DefaultHandler(ex, null, executor, repository);
        }

        abstract boolean handle();

        private static String getLoginError(WrapperException we) {
            String msg = getMessage(we);
            if(msg != null) {
                msg = msg.trim().toLowerCase();
                String msgNoSpaces;
                if(INVALID_USERNAME_OR_PASSWORD.equals(msg) ||
                   msg.contains(INVALID_USERNAME_OR_PASSWORD)
                   || INVALID_USERNAME_OR_PASSWORD_NO_SPACES.equals(msgNoSpaces = msg.replace(" ", "")) || //NOI18N
                   msgNoSpaces.contains(INVALID_USERNAME_OR_PASSWORD_NO_SPACES))
                {
                    return NbBundle.getMessage(JiraExecutor.class, "MSG_INVALID_USERNAME_OR_PASSWORD");
                } else if(msg.startsWith(REPOSITORY_LOGIN_FAILURE) ||
                         (msg.startsWith(REPOSITORY) && msg.endsWith(COULD_NOT_BE_FOUND)))
                {
                    return NbBundle.getMessage(JiraExecutor.class, "MSG_UNABLE_LOGIN_TO_REPOSITORY");
                }
            }
            return null;
        }

        private static String getMidAirColisionError(WrapperException ex) {
            String msg = getMessage(ex);
            if(msg != null) {
                msg = msg.trim().toLowerCase();
                if(msg.startsWith(MIDAIR_COLLISION)) {
                    return NbBundle.getMessage(JiraExecutor.class, "MSG_MID-AIR_COLLISION");
                }
            }
            return null;
        }

        private static String getNotFoundError(WrapperException ex) {
            if (ex.getStatus() != null) {
                IStatus status = ex.getStatus();
                Throwable t = status.getException();
                if (t instanceof UnknownHostException) {
                    return NbBundle.getMessage(JiraExecutor.class, "MSG_HOST_NOT_FOUND");
                }
                String msg = getMessage(ex);
                if (msg != null) {
                    msg = msg.trim().toLowerCase();
                    if (HTTP_ERROR_NOT_FOUND.equals(msg)) {
                        return NbBundle.getMessage(JiraExecutor.class, "MSG_HOST_NOT_FOUND");
                    }
                }
            }
            String msg = ex.getMessage();
            if (msg != null) {
                msg = msg.toLowerCase();
            }
            if (ex.getCause() instanceof JiraServiceUnavailableException
                    || HOST_NOT_FOUND_ERROR.equals(msg)) {
                return ex.getMessage();
            }
            return null;
        }


        static String getMessage(WrapperException ex) {
            String msg = ex.getMessage();
            if(msg != null && !msg.trim().equals("")) {                             // NOI18N
                return msg;
            }
            IStatus status = ex.getStatus();
            msg = status != null ? status.getMessage() : null;
            return msg != null ? msg.trim() : null;
        }

        String getMessage() {
            return erroMsg;
        }

        private static void notifyError(WrapperException ex, JiraRepository repository) {
            IStatus status = ex.getStatus();
            if (status != null && status instanceof RepositoryStatus) {
                RepositoryStatus rs = (RepositoryStatus) status;
                String html = rs.getHtmlMessage();

                if(html != null && !html.trim().equals("")) {                   // NOI18N
                    html = html.trim();
                    if(html.startsWith("<!DOCTYPE")) {                          // NOI18N
                        // <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
                        int idx = html.indexOf(">");                            // NOI18N
                        if(idx > -1) {
                            html = html.substring(idx, html.length());
                        }
                    }
                    final HtmlPanel p = new HtmlPanel();
                    String label = NbBundle.getMessage(JiraExecutor.class, "MSG_ServerResponse", new Object[] {repository.getDisplayName()}); // NOI18N
                    p.setHtml(repository.getUrl(), html, label);
                    DialogDescriptor dialogDescriptor = 
                            new DialogDescriptor(
                                p,
                                NbBundle.getMessage(JiraExecutor.class, "CTL_ServerResponse"), // NOI18N
                                true,
                                new Object[] {NotifyDescriptor.CANCEL_OPTION},
                                NotifyDescriptor.CANCEL_OPTION,
                                DialogDescriptor.DEFAULT_ALIGN,
                                new HelpCtx(p.getClass()),
                                null);

                    DialogDisplayer.getDefault().notify(dialogDescriptor);
                    return;
                }
            }
            String msg = getMessage(ex);
            JiraUtils.notifyErrorMessage(msg);
        }

        private static class LoginHandler extends ExceptionHandler {
            public LoginHandler(WrapperException ex, String msg, JiraExecutor executor, JiraRepository repository) {
                super(ex, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return erroMsg;
            }
            @Override
            protected boolean handle() {
                boolean ret = repository.authenticate(erroMsg);
                if(!ret) {
                    JiraUtils.notifyErrorMessage(NbBundle.getMessage(JiraExecutor.class, "MSG_ActionCanceledByUser")); // NOI18N
                }
                return ret;
            }
        }
        private static class NotFoundHandler extends ExceptionHandler {
            public NotFoundHandler(WrapperException ex, String msg, JiraExecutor executor, JiraRepository repository) {
                super(ex, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return erroMsg;
            }
            @Override
            protected boolean handle() {
                boolean ret = Jira.getInstance().getBugtrackingFactory().editRepository(JiraUtils.getRepository(executor.repository), erroMsg);
                if(!ret) {
                    JiraUtils.notifyErrorMessage(NbBundle.getMessage(JiraExecutor.class, "MSG_ActionCanceledByUser")); // NOI18N
                }
                return ret;
            }
        }
        private static class DefaultHandler extends ExceptionHandler {
            public DefaultHandler(WrapperException ex, String msg, JiraExecutor executor, JiraRepository repository) {
                super(ex, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return erroMsg;
            }
            @Override
            protected boolean handle() {
                if(erroMsg != null) {
                    JiraUtils.notifyErrorMessage(erroMsg);
                } else {
                    notifyError(ex, repository);
                }
                return false;
            }
        }
    }
    
    private static class WrapperException extends Exception {
        IStatus status;

        public WrapperException (String message, Throwable cause) {
            super(message, cause);
            assert cause != null;
            if (cause instanceof CoreException) {
                status = ((CoreException)cause).getStatus();
            }
        }

        /**
         * 
         * @return status of the wrapped CoreException or null if the wrapped exception is not a CoreException
         */
        public IStatus getStatus () {
            return status;
        }
    }
}

