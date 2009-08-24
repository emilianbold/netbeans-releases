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
import java.text.MessageFormat;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.autoupdate.BugzillaAutoupdate;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Executes commands against one bugzilla Repository and handles errors
 * 
 * @author Tomas Stupka
 */
public class BugzillaExecutor {

    private static final String HTTP_ERROR_NOT_FOUND         = "http error: not found";         // NOI18N
    private static final String INVALID_USERNAME_OR_PASSWORD = "invalid username or password";  // NOI18N
    private static final String REPOSITORY_LOGIN_FAILURE     = "unable to login to";            // NOI18N
    private static final String COULD_NOT_BE_FOUND           = "could not be found";            // NOI18N
    private static final String REPOSITORY                   = "repository";                    // NOI18N
    private static final String MIDAIR_COLLISION             = "mid-air collision occurred while submitting to"; // NOI18N

    private final BugzillaRepository repository;

    public BugzillaExecutor(BugzillaRepository repository) {
        this.repository = repository;
    }

    public void execute(BugzillaCommand cmd) {
        execute(cmd, true);
    }

    public void execute(BugzillaCommand cmd, boolean handleExceptions) {
        execute(cmd, handleExceptions, true);
    }

    public void execute(BugzillaCommand cmd, boolean handleExceptions, boolean checkVersion) {
        try {
            cmd.setFailed(true);

            if(checkVersion) {
                checkAutoupdate();
            }

            cmd.execute();

            cmd.setFailed(false);
            cmd.setErrorMessage(null);

        } catch (CoreException ce) {
            Bugzilla.LOG.log(Level.FINE, null, ce);

            ExceptionHandler handler = ExceptionHandler.createHandler(ce, this, repository);
            assert handler != null;

            String msg = handler.getMessage();

            cmd.setFailed(true);
            cmd.setErrorMessage(msg);

            if(handleExceptions) {
                if(handler.handle()) {
                    // execute again
                    execute(cmd);
                }
            }
            return;
                
        } catch(MalformedURLException me) {
            cmd.setErrorMessage(me.getMessage());
            Bugzilla.LOG.log(Level.SEVERE, null, me);
        } catch(IOException ioe) {
            cmd.setErrorMessage(ioe.getMessage());

            if(!handleExceptions) {
                Bugzilla.LOG.log(Level.FINE, null, ioe);
                return;
            }

            handleIOException(ioe);
        } catch(RuntimeException re) {
            Throwable t = re.getCause();
            if(t instanceof InterruptedException || !handleExceptions) {
                Bugzilla.LOG.log(Level.FINE, null, t);
            } else {
                Bugzilla.LOG.log(Level.SEVERE, null, re);
            }
        }
    }

    public boolean handleIOException(IOException io) {
        Bugzilla.LOG.log(Level.SEVERE, null, io); 
        return true;
    }

    private static abstract class ExceptionHandler {

        protected String errroMsg;
        protected CoreException ce;
        protected BugzillaExecutor executor;
        protected BugzillaRepository repository;

        protected ExceptionHandler(CoreException ce, String msg, BugzillaExecutor executor, BugzillaRepository repository) {
            this.errroMsg = msg;
            this.ce = ce;
            this.executor = executor;
            this.repository = repository;
        }

        static ExceptionHandler createHandler(CoreException ce, BugzillaExecutor executor, BugzillaRepository repository) {
            String errormsg = getLoginError(ce);
            if(errormsg != null) {
                return new LoginHandler(ce, errormsg, executor, repository);
            }
            errormsg = getNotFoundError(ce);
            if(errormsg != null) {
                return new NotFoundHandler(ce, errormsg, executor, repository);
            }
            errormsg = getMidAirColisionError(ce);
            if(errormsg != null) {
                errormsg = MessageFormat.format(errormsg, repository.getDisplayName());
                return new DefaultHandler(ce, errormsg, executor, repository);
            }
            return new DefaultHandler(ce, null, executor, repository);
        }

        abstract boolean handle();

        private static String getLoginError(CoreException ce) {
            String msg = getMessage(ce);
            if(msg != null) {
                msg = msg.trim().toLowerCase();
                if(INVALID_USERNAME_OR_PASSWORD.equals(msg) ||
                   msg.contains(INVALID_USERNAME_OR_PASSWORD))
                {
                    return NbBundle.getMessage(BugzillaExecutor.class, "MSG_INVALID_USERNAME_OR_PASSWORD"); 
                } else if(msg.startsWith(REPOSITORY_LOGIN_FAILURE) ||
                         (msg.startsWith(REPOSITORY) && msg.endsWith(COULD_NOT_BE_FOUND)))
                {
                    return NbBundle.getMessage(BugzillaExecutor.class, "MSG_UNABLE_LOGIN_TO_REPOSITORY"); 
                }
            }
            return null;
        }

        private static String getMidAirColisionError(CoreException ce) {
            String msg = getMessage(ce);
            if(msg != null) {
                msg = msg.trim().toLowerCase();
                if(msg.startsWith(MIDAIR_COLLISION)) {
                    return NbBundle.getMessage(BugzillaExecutor.class, "MSG_MID-AIR_COLLISION"); 
                }
            }
            return null;
        }

        private static String getNotFoundError(CoreException ce) {
            IStatus status = ce.getStatus();
            Throwable t = status.getException();
            if(t instanceof UnknownHostException) {
                return NbBundle.getMessage(BugzillaExecutor.class, "MSG_HOST_NOT_FOUND");
            }
            String msg = getMessage(ce);
            if(msg != null) {
                msg = msg.trim().toLowerCase();
                if(HTTP_ERROR_NOT_FOUND.equals(msg)) {
                    return NbBundle.getMessage(BugzillaExecutor.class, "MSG_HOST_NOT_FOUND");
                }
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

        private static void notifyError(CoreException ce, BugzillaRepository repository) {
            IStatus status = ce.getStatus();
            if (status instanceof RepositoryStatus) {
                RepositoryStatus rs = (RepositoryStatus) status;
                String html = rs.getHtmlMessage();
                if(html != null && !html.trim().equals("")) {                   // NOI18N
                    html = parseHtmlMessage(html);
                    final HtmlPanel p = new HtmlPanel();
                    String label = NbBundle.getMessage(BugzillaExecutor.class, "MSG_ServerResponse", new Object[] {repository.getDisplayName()}); // NOI18N
                    p.setHtml(repository.getUrl(), html, label);
                    DialogDescriptor dialogDescriptor = 
                            new DialogDescriptor(
                                p,
                                NbBundle.getMessage(BugzillaExecutor.class, "CTL_ServerResponse"), // NOI18N
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
            String msg = getMessage(ce);
            notifyErrorMessage(msg);
        }

        @SuppressWarnings("empty-statement")
        private static String parseHtmlMessage(String html) {
            int idxS = html.indexOf("<div id=\"bugzilla-body\">");              // NOI18N
            if(idxS < 0) {
                return html;
            }
            int idx = idxS;
            int idxE = html.indexOf("</div>", idx);                             // NOI18N
            int levels = 1;
            while(true) {
                idx = html.indexOf("<div", idx + 1);                            // NOI18N
                if(idx < 0 || idx > idxE) {
                    break;
                }
                levels++;
            }

            idxE = idxS;
            for (int i = 0; i < levels; i++) {
                idxE = html.indexOf("</div>", idxE + 1);                        // NOI18N
            }
            idxE = idxE > 6 ? idxE + 6 : html.length();
            html = html.substring(idxS, idxE);

            // very nice
            html = html.replaceAll("Please press \\<b\\>Back\\</b\\> and try again.", ""); // NOI18N

            return html;
        }

        static void notifyErrorMessage(String msg) {
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

        private static class LoginHandler extends ExceptionHandler {
            public LoginHandler(CoreException ce, String msg, BugzillaExecutor executor, BugzillaRepository repository) {
                super(ce, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                boolean ret = repository.authenticate(errroMsg);
                if(!ret) {
                    notifyErrorMessage(NbBundle.getMessage(BugzillaExecutor.class, "MSG_ActionCanceledByUser")); // NOI18N
                }
                return ret;
            }
        }
        private static class NotFoundHandler extends ExceptionHandler {
            public NotFoundHandler(CoreException ce, String msg, BugzillaExecutor executor, BugzillaRepository repository) {
                super(ce, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                boolean ret = BugtrackingUtil.editRepository(executor.repository, errroMsg);
                if(!ret) {
                    notifyErrorMessage(NbBundle.getMessage(BugzillaExecutor.class, "MSG_ActionCanceledByUser")); // NOI18N
                }
                return ret;
            }
        }
        private static class DefaultHandler extends ExceptionHandler {
            public DefaultHandler(CoreException ce, String msg, BugzillaExecutor executor, BugzillaRepository repository) {
                super(ce, msg, executor, repository);
            }
            @Override
            String getMessage() {
                return errroMsg;
            }
            @Override
            protected boolean handle() {
                if(errroMsg != null) {
                    notifyErrorMessage(errroMsg);
                } else {
                    notifyError(ce, repository);
                }
                return false;
            }
        }
    }

    private void checkAutoupdate() {
        try {
            BugzillaAutoupdate jau = new BugzillaAutoupdate();
            jau.checkAndNotify(repository);
        } catch (Throwable t) {
            Bugzilla.LOG.log(Level.SEVERE, "Exception in Bugzilla autoupdate check.", t);
        }
    }
}

