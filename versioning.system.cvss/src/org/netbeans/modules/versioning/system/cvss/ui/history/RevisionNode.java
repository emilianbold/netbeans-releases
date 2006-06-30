/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.text.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.netbeans.modules.versioning.system.cvss.IllegalCommandException;
import org.netbeans.modules.versioning.system.cvss.NotVersionedException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class RevisionNode extends AbstractNode {
    
    static final String COLUMN_NAME_NAME        = "name"; // NOI18N
    static final String COLUMN_NAME_DATE        = "date"; // NOI18N
    static final String COLUMN_NAME_USERNAME    = "username"; // NOI18N
    static final String COLUMN_NAME_MESSAGE     = "message"; // NOI18N
        
    private LogInformation.Revision                 revision;
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
        super(revision.getChildren() == null ? Children.LEAF : new RevisionNodeChildren(revision), Lookups.fixed(new Object [] { revision }));
        this.path = revision.getPath();
        this.revision = revision.getRevision();
        setName(revision.getRevision().getNumber());
        initProperties();
    }

    LogInformation.Revision getRevision() {
        return revision;
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
        if (revision == null) {
            return new Action [0];
        } else {
            return new Action [] {
                new RollbackAction(),
                new RollbackChangeAction(),
                new FindCommitAction(false),
                new FindCommitAction(true),
            };
        }
    }
    
    public Node.Cookie getCookie(Class clazz) {
        
        if (ViewCookie.class.equals(clazz)) {
            File file = revision.getLogInfoHeader().getFile();

            String mime = null;
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                mime = fo.getMIMEType();
            }
            ViewEnv env = new ViewEnv(file, revision.getNumber().trim(), mime);
            return new ViewCookieImpl(env);
        } else {
            return super.getCookie(clazz);
        }
    }
    
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new DateProperty());
        ps.put(new UsernameProperty());
        ps.put(new MessageProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    /**
     * OpenSupport that is able to open an input stream.
     * Encoding, coloring, ..., let editor kit takes care
     */
    private class ViewCookieImpl extends CloneableEditorSupport implements ViewCookie {

        ViewCookieImpl(Env env) {
            super(env);
        }
                                
        protected String messageName() {
            return revision.getLogInfoHeader().getFile().getName() + " " + getName();
        }
        
        protected String messageSave() {
            return revision.getLogInfoHeader().getFile().getName() + " " + getName();
        }
        
        protected java.lang.String messageToolTip() {
            return revision.getLogInfoHeader().getFile().getName() + " " + getName();
        }

        protected java.lang.String messageOpening() {
            return  NbBundle.getMessage(RevisionNode.class, "CTL_Action_Opening", revision.getLogInfoHeader().getFile().getName() + " " + getName());
        }
        
        protected java.lang.String messageOpened() {
            return "";
        }

        //#20646 associate the entry node with editor top component
        protected CloneableEditor createCloneableEditor() {
            CloneableEditor editor = super.createCloneableEditor();
            editor.setActivatedNodes(new Node[] {RevisionNode.this});
            return editor;
        }

        private Object writeReplace() {
            return null;
        }
                
    }    
        
    private class ViewEnv extends FileEnvironment {

        /** Serial Version UID */
        private static final long serialVersionUID = 1L;
        
        ViewEnv (File file, String revision, String mime) {
            super(file, revision, mime);
        }

        public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
            return (ViewCookieImpl) RevisionNode.this.getCookie(ViewCookie.class);
        }
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
    
    private class UsernameProperty extends CommitNodeProperty {

        public UsernameProperty() {
            super(COLUMN_NAME_USERNAME, String.class, COLUMN_NAME_USERNAME, COLUMN_NAME_USERNAME);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (revision != null) {
                return revision.getAuthor();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private class DateProperty extends CommitNodeProperty {

        public DateProperty() {
            super(COLUMN_NAME_DATE, String.class, COLUMN_NAME_DATE, COLUMN_NAME_DATE);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (revision != null) {
                return revision.getDateString();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private class MessageProperty extends CommitNodeProperty {

        public MessageProperty() {
            super(COLUMN_NAME_MESSAGE, String.class, COLUMN_NAME_MESSAGE, COLUMN_NAME_MESSAGE);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (revision != null) {
                return revision.getMessage();
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
                putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_FindCommitInProjects"));
            } else {
                File file = revision.getLogInfoHeader().getFile();
                Project project = Utils.getProject(file);
                if (project != null) {
                    String prjName = ProjectUtils.getInformation(project).getDisplayName();
                    putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_FindCommitInProject", prjName));
                } else {
                    putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_FindCommit"));
                    setEnabled(false);
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            File file = revision.getLogInfoHeader().getFile();
            if (allProjects) {
                Project [] projects  = OpenProjects.getDefault().getOpenProjects();
                int n = projects.length;
                SearchHistoryAction.openSearch(
                        (n == 1) ? ProjectUtils.getInformation(projects[0]).getDisplayName() : 
                        NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_OpenProjects_Title", Integer.toString(n)),
                        revision.getMessage().trim(), revision.getAuthor(), revision.getDate());
            } else {
                Project project = Utils.getProject(file);                
                Context context = Utils.getProjectsContext(new Project[] { project });
                SearchHistoryAction.openSearch(
                        context, 
                        ProjectUtils.getInformation(project).getDisplayName(),
                        revision.getMessage().trim(), revision.getAuthor(), revision.getDate());
            }
        }
    }

    private class RollbackAction extends AbstractAction {

        public RollbackAction() {
            putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackTo", revision.getNumber()));
        }

        public void actionPerformed(ActionEvent e) {
            File file = revision.getLogInfoHeader().getFile();
            GetCleanAction.rollback(file, revision.getNumber());
        }
    }

    private class RollbackChangeAction extends AbstractAction {

        public RollbackChangeAction() {
            putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackChange"));
            setEnabled(Utils.previousRevision(revision.getNumber()) != null);
        }

        public void actionPerformed(ActionEvent e) {
            SummaryView.rollbackChanges(new LogInformation.Revision [] { revision });
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
}
