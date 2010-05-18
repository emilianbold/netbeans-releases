/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.DateFormat;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class RevisionNode extends AbstractNode {
    
    static final String COLUMN_NAME_NAME        = "name"; // NOI18N
    static final String COLUMN_NAME_LOCATION    = "location"; // NOI18N
    static final String COLUMN_NAME_DATE        = "date"; // NOI18N
    static final String COLUMN_NAME_USERNAME    = "username"; // NOI18N
    static final String COLUMN_NAME_TAGS        = "tags"; // NOI18N
    static final String COLUMN_NAME_MESSAGE     = "message"; // NOI18N
        
    private SearchHistoryPanel.DispRevision         revision;
    private SearchHistoryPanel.ResultsContainer     container;
    private String                                  path;

    public RevisionNode(SearchHistoryPanel.ResultsContainer container) {
        super(new RevisionNodeChildren(container), Lookups.singleton(container));
        this.container = container;
        this.revision = null;
        this.path = container.getPath();
        setName(((SearchHistoryPanel.DispRevision) container.getRevisions().get(0)).getRevision().getLogInfoHeader().getFile().getName());
        initProperties();
    }

    public RevisionNode(SearchHistoryPanel.DispRevision revision) {
        super(revision.getChildren() == null ? Children.LEAF : new RevisionNodeChildren(revision), Lookups.fixed(revision));
        this.path = revision.getPath();
        this.revision = revision;
        if (revision.getRevision().getNumber() == VersionsCache.REVISION_CURRENT) {
            setName(NbBundle.getMessage(DiffResultsView.class, "LBL_DiffPanel_LocalCopy"));  // NOI18N
        } else if (revision.isBranchRoot()) {
            if (revision.getBranchName() != null) {
                setName(revision.getRevision().getNumber() + " - " + revision.getBranchName());  // NOI18N
            } else {
                setName(revision.getRevision().getNumber());
            }
        } else {
            setName(revision.getRevision().getNumber());
        }
        initProperties();
    }

    SearchHistoryPanel.DispRevision getDispRevision() {
        return revision;
    }

    LogInformation.Revision getRevision() {
        return revision.getRevision();
    }

    SearchHistoryPanel.ResultsContainer getContainer() {
        return container;
    }

    public String getShortDescription() {
        return path;
    }

    public Action[] getActions(boolean context) {
        if (context) return null;
        // TODO: reuse action code from SummaryView
        if (revision == null || revision.getRevision().getNumber() == VersionsCache.REVISION_CURRENT) {
            return new Action [0];
        } else {
            if (!revision.isBranchRoot()) {
            return new Action [] {
                new RollbackAction(),
                new RollbackChangeAction(),
                new OpenRevisionAction(),
                new FindCommitAction(false),
                new FindCommitAction(true),
            };
            } else {
                return new Action [0];
        }
    }
    }
    
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new LocationProperty());
        ps.put(new DateProperty());
        ps.put(new UsernameProperty());
        ps.put(new TagsProperty());
        ps.put(new MessageProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    private abstract class CommitNodeProperty extends PropertySupport.ReadOnly {

        protected CommitNodeProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return e.getLocalizedMessage();
            }
        }

        public PropertyEditor getPropertyEditor() {
            try {
                return new RevisionPropertyEditor((String) getValue());
            } catch (Exception e) {
                return super.getPropertyEditor();
            }
        }
    }
    
    private class LocationProperty extends CommitNodeProperty {

        public LocationProperty() {
            super(COLUMN_NAME_LOCATION, String.class, COLUMN_NAME_LOCATION, COLUMN_NAME_LOCATION);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (container != null) {
                return path;
            } else {
                return ""; // NOI18N
            }
        }
    }
    
    private class UsernameProperty extends CommitNodeProperty {

        public UsernameProperty() {
            super(COLUMN_NAME_USERNAME, String.class, COLUMN_NAME_USERNAME, COLUMN_NAME_USERNAME);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (revision != null) {
                return revision.getRevision().getAuthor();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private class DateProperty extends CommitNodeProperty {
        
        private String dateString;

        public DateProperty() {
            super(COLUMN_NAME_DATE, String.class, COLUMN_NAME_DATE, COLUMN_NAME_DATE);
            dateString = (revision == null || revision.getRevision().getDate() == null) ? "" : DateFormat.getDateTimeInstance().format(revision.getRevision().getDate()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return dateString;
        }
    }

    private class TagsProperty extends CommitNodeProperty {

        public TagsProperty() {
            super(COLUMN_NAME_TAGS, List.class, COLUMN_NAME_TAGS, COLUMN_NAME_TAGS);
            if (revision != null) setValue("tagsRevision", revision);  // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return null; // nobody reads this, the custom editor handles painting, see below
        }

        public PropertyEditor getPropertyEditor() {
            try {
                return new TagsPropertyEditor(revision);
            } catch (Exception e) {
                return super.getPropertyEditor();
            }
        }        
    }
    
    private class MessageProperty extends CommitNodeProperty {

        public MessageProperty() {
            super(COLUMN_NAME_MESSAGE, String.class, COLUMN_NAME_MESSAGE, COLUMN_NAME_MESSAGE);
            if (revision != null && revision.getRevision().getMessage() != null) setValue("messageRevision", revision);  // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (revision != null) {
                return revision.getRevision().getMessage();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private class FindCommitAction extends AbstractAction {

        private boolean allProjects;

        public FindCommitAction(boolean allProjects) {
            this.allProjects = allProjects;
            if (allProjects) {
                putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_FindCommitInProjects"));  // NOI18N
            } else {
                File file = revision.getRevision().getLogInfoHeader().getFile();
                Project project = Utils.getProject(file);
                if (project != null) {
                    String prjName = ProjectUtils.getInformation(project).getDisplayName();
                    putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_FindCommitInProject", prjName));  // NOI18N
                } else {
                    putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_FindCommit"));  // NOI18N
                    setEnabled(false);
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            File file = revision.getRevision().getLogInfoHeader().getFile();
            if (allProjects) {
                Project [] projects  = OpenProjects.getDefault().getOpenProjects();
                int n = projects.length;
                SearchHistoryAction.openSearch(
                        (n == 1) ? ProjectUtils.getInformation(projects[0]).getDisplayName() : 
                        NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_OpenProjects_Title", Integer.toString(n)),  // NOI18N
                        revision.getRevision().getMessage().trim(), revision.getRevision().getAuthor(), revision.getRevision().getDate());
            } else {
                Project project = Utils.getProject(file);                
                Context context = Utils.getProjectsContext(new Project[] { project });
                SearchHistoryAction.openSearch(
                        context, 
                        ProjectUtils.getInformation(project).getDisplayName(),
                        revision.getRevision().getMessage().trim(), revision.getRevision().getAuthor(), revision.getRevision().getDate());
            }
        }
    }

    private class OpenRevisionAction extends AbstractAction {

        public OpenRevisionAction() {
            putValue(Action.NAME, NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View", revision.getRevision().getNumber()));  // NOI18N
        }

        public boolean isEnabled() {
            return !"dead".equals(revision.getRevision().getState());
        }

        public void actionPerformed(ActionEvent ex) {
            try {
                ViewRevisionAction.view(revision.getRevision().getLogInfoHeader().getFile(), revision.getRevision().getNumber(), null);
            } catch (Exception e) {
                Logger.getLogger(RevisionNode.class.getName()).log(Level.INFO, e.getMessage(), e);
            }
        }
    }
    
    private class RollbackAction extends AbstractAction {

        public RollbackAction() {
            putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackTo", revision.getRevision().getNumber()));  // NOI18N
        }

        public boolean isEnabled() {
            return !"dead".equals(revision.getRevision().getState());
        }

        public void actionPerformed(ActionEvent e) {
            File file = revision.getRevision().getLogInfoHeader().getFile();
            GetCleanAction.rollback(file, revision.getRevision().getNumber());
        }
    }

    private class RollbackChangeAction extends AbstractAction {

        public RollbackChangeAction() {
            putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackChange"));  // NOI18N
            setEnabled(Utils.previousRevision(revision.getRevision().getNumber()) != null);
        }

        public void actionPerformed(ActionEvent e) {
            SummaryView.rollbackChanges(new LogInformation.Revision [] { revision.getRevision() });
        }
    }
    
    private static class RevisionPropertyEditor extends PropertyEditorSupport {

        private static final JLabel renderer = new JLabel();

        static {
            renderer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }

        public RevisionPropertyEditor(String value) {
            setValue(value);
        }

        public void paintValue(Graphics gfx, Rectangle box) {
            renderer.setForeground(gfx.getColor());
            renderer.setText((String) getValue());
            renderer.setBounds(box);
            renderer.paint(gfx);
        }

        public boolean isPaintable() {
            return true;
        }
    }
    
    private static class TagsPropertyEditor extends PropertyEditorSupport {

        private static final JLabel renderer = new JLabel();
        
        private SearchHistoryPanel.DispRevision dispRevision;

        static {
            renderer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }
        
        public TagsPropertyEditor(SearchHistoryPanel.DispRevision revision) {
            this.dispRevision = revision;
        }

        public boolean isPaintable() {
            return true;
        }

        public void paintValue(Graphics gfx, Rectangle box) {
            renderer.setForeground(gfx.getColor());
            renderer.setBounds(box);

            if (dispRevision == null || dispRevision.getBranches() == null) {
                renderer.setText(""); // NOI18N
            } else {
                List<String> tags = new ArrayList<String>(dispRevision.getBranches());
                tags.addAll(dispRevision.getTags());
                if (tags.size() > 0) {
                    String tagInfo = "<html>" + tags.get(0); // NOI18N
                    if (tags.size() > 1) {
                        tagInfo += ","; // NOI18N
                        Color foreground = UIManager.getColor("List.selectionForeground"); // NOI18N
                        if (!gfx.getColor().equals(foreground)) {
                            tagInfo += " <a href=\"\">...</a>"; // NOI18N
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append(" <a href=\"\" style=\"color:"); // NOI18N
                            sb.append("rgb("); // NOI18N
                            sb.append(foreground.getRed());
                            sb.append(","); // NOI18N
                            sb.append(foreground.getGreen());
                            sb.append(","); // NOI18N
                            sb.append(foreground.getBlue());
                            sb.append(")"); // NOI18N
                            sb.append("\">"); // NOI18N
                            sb.append("...");  // NOI18N
                            sb.append("</a>"); // NOI18N
                            tagInfo += sb.toString();
                        }
                    }
                    renderer.setText(tagInfo);
                } else {
                    renderer.setText("");  // NOI18N
                }
            }
            renderer.paint(gfx);
        }
    }
}
