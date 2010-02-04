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

package org.netbeans.modules.bugzilla.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.commands.BugzillaCommand;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class BugzillaUtil {

    public static boolean show(JPanel panel, String title, String okName) {
        return show(panel, title, okName, new HelpCtx(panel.getClass()));
    }

    public static boolean show(JPanel panel, String title, String okName, HelpCtx helpCtx) {
        JButton ok = new JButton(okName);
        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        JButton cancel = new JButton(NbBundle.getMessage(BugzillaUtil.class, "LBL_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());
        DialogDescriptor descriptor = new DialogDescriptor (
                panel,
                title,
                true,
                new Object[] {ok, cancel},
                ok,
                DialogDescriptor.DEFAULT_ALIGN,
                helpCtx,
                null);
        return DialogDisplayer.getDefault().notify(descriptor) == ok;
    }

    /**
     * Returns TaskData for the given issue id or null if an error occured
     * @param repository
     * @param id
     * @return
     */
    public static TaskData getTaskData(final BugzillaRepository repository, final String id) {
        return getTaskData(repository, id, true);
    }

    /**
     * Returns TaskData for the given issue id or null if an error occured
     * @param repository
     * @param id
     * @return
     */
    public static TaskData getTaskData(final BugzillaRepository repository, final String id, boolean handleExceptions) {
        final TaskData[] taskData = new TaskData[1];
        BugzillaCommand cmd = new BugzillaCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                taskData[0] = Bugzilla.getInstance().getRepositoryConnector().getTaskData(repository.getTaskRepository(), id, new NullProgressMonitor());
            }
        };
        repository.getExecutor().execute(cmd, handleExceptions);
        if(cmd.hasFailed() && Bugzilla.LOG.isLoggable(Level.FINE)) {
            Bugzilla.LOG.log(Level.FINE, cmd.getErrorMessage());
        }
        return taskData[0];
    }

    public static String getKeywords(String label, String keywordsString, BugzillaRepository repository) {
        String[] keywords = keywordsString.split(","); // NOI18N
        if(keywords == null || keywords.length == 0) {
            return null;
        }

        KeywordsPanel kp;
        try {
            BugzillaConfiguration bc = repository.getConfiguration();
            if(bc == null || !bc.isValid()) {
                // XXX is there something else we could do at this point?
                return keywordsString;
            }
            List<String> knownKeywords = bc.getKeywords();
            kp = new KeywordsPanel(label, knownKeywords, keywords);
        } catch (Exception ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
            return keywordsString;
        }       

        ResourceBundle bundle = NbBundle.getBundle(BugzillaUtil.class);
        if (BugzillaUtil.show(kp, bundle.getString("LBL_Keywords"), bundle.getString("LBL_Ok"))) { // NOI18N
            String[] values = kp.getSelectedKeywords();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                String s = values[i];
                sb.append(s);
                if(i < values.length - 1) {
                    sb.append(", "); // NOI18N
                }
            }
            return sb.toString();
        }
        return keywordsString;
    }

    public static boolean isAssertEnabled() {
        boolean retval = false;
        assert retval = true;
        return retval;
    }

    /**
     * Determines wheter the given {@link Repository} is the
     * repository hosting netbeans or not
     *
     * @param repo
     * @return true if the given repository is the netbenas bugzilla, otherwise false
     */
    public static boolean isNbRepository(Repository repo) {
        return BugtrackingUtil.isNbRepository(repo);
    }

    public static boolean showQAContact(BugzillaRepository repo) {
        return isNbRepository(repo) || !(repo instanceof KenaiRepository);
    }

    public static boolean showStatusWhiteboard(BugzillaRepository repo) {
        return isNbRepository(repo) || !(repo instanceof KenaiRepository);
    }

    public static boolean showIssueType(BugzillaRepository repo) {
        return isNbRepository(repo);
    }

}
