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

import java.util.List;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaRepository;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaUtil {
    public static boolean show(JPanel panel, String title, String okName) {
        JButton ok = new JButton(okName);
        JButton cancel = new JButton(NbBundle.getMessage(BugzillaUtil.class, "LBL_Cancel"));
        NotifyDescriptor descriptor = new NotifyDescriptor (
                panel,
                title,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                new Object [] { ok, cancel },
                ok);
        return DialogDisplayer.getDefault().notify(descriptor) == ok;
    }

    public static void performQuery(TaskRepository taskRepository, String queryUrl, TaskDataCollector collector)  {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), "");
        query.setUrl(queryUrl);
        BugzillaRepositoryConnector rc = Bugzilla.getInstance().getRepositoryConnector();
        rc.performQuery(taskRepository, query, collector, null, new NullProgressMonitor());
    }

    public static String getKeywords(String label, String keywordsString, BugzillaRepository repository) {
        String[] keywords = keywordsString.split(","); // NOI18N
        if(keywords == null || keywords.length == 0) {
            return null;
        }

        KeywordsPanel kp;
        try {
            List<String> knownKeywords = Bugzilla.getInstance().getKeywords(repository);
            kp = new KeywordsPanel(label, knownKeywords, keywords);
        } catch (Exception ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
            return null;
        }       

        if(BugzillaUtil.show(kp, label, NbBundle.getMessage(BugzillaUtil.class, "LBL_Select"))) {
            Object[] values = kp.keywordsList.getSelectedValues();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < values.length; i++) {
                String s = (String) values[i];
                sb.append(s);
                if(i < values.length - 1) {
                    sb.append(", "); // NOI18N
                }
            }
            return sb.toString();
        }
        return null;
    }
}
