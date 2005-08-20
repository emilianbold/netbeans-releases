/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.nodes.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.ErrorManager;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.file.FileUtils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class RevisionNode extends AbstractNode {
    
    static final String COLUMN_NAME_NAME        = "name";
    static final String COLUMN_NAME_REVISION    = "revision";
    static final String COLUMN_NAME_DATE        = "date";
    static final String COLUMN_NAME_USERNAME    = "username";
    static final String COLUMN_NAME_MESSAGE     = "message";
    static final String COLUMN_NAME_PATH        = "path";
        
    private LogInformation.Revision                 revision;
    private SearchHistoryPanel.ResultsContainer     container;

    public RevisionNode(SearchHistoryPanel.ResultsContainer container) {
        super(new RevisionNodeChildren(container), Lookups.singleton(container));
        this.container = container;
        this.revision = null;
        initProperties();
    }

    public RevisionNode(LogInformation.Revision revision) {
        super(Children.LEAF, Lookups.fixed(new Object [] { revision }));
        this.revision = revision;
        initProperties();
    }

    LogInformation.Revision getRevision() {
        return revision;
    }

    SearchHistoryPanel.ResultsContainer getContainer() {
        return container;
    }

    public String getName() {
        LogInformation.Revision rev = revision != null ? revision : (LogInformation.Revision) container.getRevisions().get(0);
        return rev.getLogInfoHeader().getFile().getName();
    }

    public Action[] getActions(boolean context) {
        if (context) return null;
        // TODO: reuse action code from SummaryView
        if (revision == null) {
            return new Action [0];
        } else {
            return new Action [] {
                new RollbackAction(),
                new FindCommitAction(false),
                new FindCommitAction(true),
            };
        }
    }
    
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new RevisionProperty());
        ps.put(new DateProperty());
        ps.put(new UsernameProperty());
        ps.put(new MessageProperty());
        ps.put(new PathProperty());
        
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
                return "<error>";
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
    
    private class RevisionProperty extends CommitNodeProperty {

        public RevisionProperty() {
            super(COLUMN_NAME_REVISION, String.class, COLUMN_NAME_REVISION, COLUMN_NAME_REVISION);
        }

        public Object getValue() {
            if (revision != null) {
                return revision.getNumber();
            } else {
                List revs = container.getRevisions();
                LogInformation.Revision newest = (LogInformation.Revision) revs.get(0);
                if (container.getEldestRevision() == null) {
                    return newest.getNumber();
                } else {
                    return container.getEldestRevision() + " - " +  newest.getNumber();
                }
            }
        }
    }

    private class PathProperty extends CommitNodeProperty {

        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, COLUMN_NAME_PATH, COLUMN_NAME_PATH);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return "";
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
                return "";
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
                return "";
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
                return "";
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
                SearchHistoryAction.openSearch(NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_Title", file.getName(), revision.getNumber()),
                                               revision.getMessage().trim(), revision.getAuthor(), revision.getDate());
            } else {
                Context context = Utils.getProjectsContext(new Project[] { Utils.getProject(file) });
                SearchHistoryAction.openSearch(context, NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_Title", file.getName(), revision.getNumber()),
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
            int res = JOptionPane.showConfirmDialog(
                    null,
                    NbBundle.getMessage(RevisionNode.class, "CTL_Rollback_Prompt", file.getName(), revision.getNumber()),
                    NbBundle.getMessage(RevisionNode.class, "CTL_Rollback_Title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (res != JOptionPane.YES_OPTION) return;

            try {
                File cleanFile = VersionsCache.getInstance().getRemoteFile(file, revision.getNumber());
                FileUtils.copyFile(cleanFile, file);
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    fo.refresh();
                }
                CvsVersioningSystem.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            } catch (Exception e1) {
                ErrorManager.getDefault().notify(e1);
            }
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
