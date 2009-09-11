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

package org.netbeans.modules.bugtracking.bridge.exportdiff;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchComboBar;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.versioning.util.ExportDiffSupport;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.ExportDiffSupport.ExportDiffProvider.class)
public class AttachIssueProvider extends ExportDiffSupport.ExportDiffProvider implements DocumentListener, PropertyChangeListener {

    private BugtrackingOwnerSupport support;
    private AttachPanel panel;
    private File[] files;
    private static Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.exportdiff.AttachIssueProvider");   // NOI18N

    public AttachIssueProvider() {
        support = BugtrackingOwnerSupport.getInstance();
    }

    @Override
    protected void setContext(File[] files) {
        this.files = files;
    }

    @Override
    public void handleDiffFile(File file) {
        LOG.log(Level.FINE, "handeDiff start for " + file); // NOI18N

        Issue issue = panel.getIssue();
        if (issue == null) {
            LOG.log(Level.FINE, " no issue set"); // NOI18N
            return;
        }
        
        issue.attachPatch(file, panel.descriptionTextField.getText());
        issue.open();

        LOG.log(Level.FINE, "handeDiff end for " + file); // NOI18N
    }

    @Override
    public JComponent createComponent() {
        assert files != null;
        panel = new AttachPanel(this);
        panel.descriptionTextField.getDocument().addDocumentListener(this);        
        panel.init(files.length > 0 ? files[0] : null);
        return panel;
    }

    @Override
    public boolean isValid() {
        return !panel.descriptionTextField.getText().trim().equals("") &&       // NOI18N
                panel.getIssue() != null;
    }

    public void insertUpdate(DocumentEvent e)  { fireDataChanged(); }
    public void removeUpdate(DocumentEvent e)  { fireDataChanged(); }
    public void changedUpdate(DocumentEvent e) { fireDataChanged(); }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(QuickSearchComboBar.EVT_ISSUE_CHANGED)) {
            fireDataChanged();
        }
    }
}
