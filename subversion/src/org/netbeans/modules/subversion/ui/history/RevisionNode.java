/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.history;

import org.openide.nodes.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditor;
import org.openide.ErrorManager;
import org.netbeans.modules.subversion.util.SvnUtils;

import javax.swing.*;
import java.io.File;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.text.DateFormat;

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
                //new RollbackAction(),
                new RollbackChangeAction(),
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
                return DateFormat.getDateTimeInstance().format(revision.getDate());
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

    private class RollbackAction extends AbstractAction {

        public RollbackAction() {
            putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackTo", revision.getNumber()));
        }

        public void actionPerformed(ActionEvent e) {
            File file = revision.getLogInfoHeader().getFile();
//            GetCleanAction.rollback(file, revision.getNumber());
        }
    }

    private class RollbackChangeAction extends AbstractAction {

        public RollbackChangeAction() {
            putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackChange"));
            setEnabled(SvnUtils.previousRevision(revision.getNumber()) != null);
        }

        public void actionPerformed(ActionEvent e) {
//            SummaryView.rollbackChanges(new LogInformation.Revision [] { revision });
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
