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

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaStatus;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaRepository;
import org.netbeans.modules.bugzilla.RepositoryPanel;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Executes commands against one bugzilla Repository
 * 
 * @author Tomas Stupka
 */
public class BugzillaExecutor {
    public static final String HTTP_ERROR_NOT_FOUND = "Http error: Not Found";

    private static final String INVALID_USERNAME_OR_PASSWORD = "invalid username or password";

    private final BugzillaRepository repository;

    public BugzillaExecutor(BugzillaRepository repository) {
        this.repository = repository;
    }

    public void execute(BugzillaCommand cmd) {
        try {
            cmd.execute();
        } catch (CoreException ex) {
            if(handleException(ex)) {
                execute(cmd);
            } else {
                notifyError(ex);
            }
        } catch(MalformedURLException me) {
            Bugzilla.LOG.log(Level.SEVERE, null, me);
        } catch(IOException ioe) {
            Bugzilla.LOG.log(Level.SEVERE, null, ioe);
        } 
    }

    public static boolean isAuthenticate(CoreException ce) {
        return containsMsg(ce, INVALID_USERNAME_OR_PASSWORD);
    }

    public static boolean isNotFound(CoreException ce) {
        return containsMsg(ce, HTTP_ERROR_NOT_FOUND);
    }

    private static boolean containsMsg(CoreException ce, String cotainsMsg) {
        String msg = ce.getMessage();
        if(cotainsMsg.equals(msg)) {
            return true;
        }
        IStatus status = ce.getStatus();
        msg = status != null ? status.getMessage() : null;
        msg = msg != null ? msg.trim() : msg;
        return cotainsMsg.equals(msg); // XXX
    }

    public boolean handleException(CoreException ce) {
        if(isAuthenticate(ce)) {
            return handleAuthenticate(ce);
        }
        return false;
    }

    private boolean handleAuthenticate(CoreException ce) {
        boolean edit = BugtrackingUtil.editRepository(repository);
        if(edit) {
            Bugzilla.getInstance().removeRepository(repository);
        }
        return edit;
    }

    private void notifyError(CoreException ce) {
        IStatus status = ce.getStatus();
        if (status instanceof RepositoryStatus) {
            RepositoryStatus rs = (RepositoryStatus) status;
            String html = rs.getHtmlMessage();
            if(html != null && !html.trim().equals("")) {
                final HtmlPanel p = new HtmlPanel();
                p.setHtml(html);
                BugzillaUtil.show(p, "html", "ok");
//                 XXX show in browser ?
            }
        } else if (status instanceof BugzillaStatus) {
            BugzillaStatus bs = (BugzillaStatus) status;
            String msg = bs.getMessage();
            notifyErrorMessage(msg);
        } else {
            String msg = status.getMessage();
            notifyErrorMessage(msg);
        }
    }

    private void notifyErrorMessage(String msg) {
        NotifyDescriptor nd =
                new NotifyDescriptor(
                    msg, 
                    NbBundle.getMessage(BugzillaExecutor.class, "LBLError"),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
    }
}

