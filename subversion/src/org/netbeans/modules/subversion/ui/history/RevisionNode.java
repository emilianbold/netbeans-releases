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
        
    private RepositoryRevision.Event    event;
    private RepositoryRevision          container;
    private String                      path;

    public RevisionNode(RepositoryRevision container) {
        super(new RevisionNodeChildren(container), Lookups.singleton(container));
        this.container = container;
        this.event = null;
        this.path = null;
        setName(container.getLog().getRevision().getNumber() +
                NbBundle.getMessage(RevisionNode.class, "LBL_NumberOfChangedPaths", container.getLog().getChangedPaths().length));
        initProperties();
    }

    public RevisionNode(RepositoryRevision.Event revision) {
        super(Children.LEAF, Lookups.fixed(new Object [] { revision }));
        this.path = revision.getChangedPath().getPath();
        this.event = revision;
        setName(revision.getName());
        initProperties();
    }

    RepositoryRevision.Event getRevision() {
        return event;
    }

    RepositoryRevision getContainer() {
        return container;
    }

    public String getShortDescription() {
        return path;
    }

    public Action[] getActions(boolean context) {
        if (context) return null;
        // TODO: reuse action code from SummaryView
        if (event == null) {
            return new Action [] {
                new RevertModificationsAction()
            };
        } else {
            return new Action [] {
                new RollbackAction(),
                new RevertModificationsAction(),
            };
        }
    }
    
    public Node.Cookie getCookie(Class clazz) {
        
        if (ViewCookie.class.equals(clazz)) {
            File file = event.getFile();

            String mime = null;
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                mime = fo.getMIMEType();
            }
            ViewEnv env = new ViewEnv(file, Long.toString(event.getLogInfoHeader().getLog().getRevision().getNumber()), mime);
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

        private String formatName() {
            return getName() + " @ " + event.getLogInfoHeader().getLog().getRevision().getNumber(); // NOI18N
        }

        protected String messageName() {
            return formatName();
        }

        protected String messageSave() {
            return formatName();
        }
        
        protected java.lang.String messageToolTip() {
            return formatName();
        }

        protected java.lang.String messageOpening() {
            return  NbBundle.getMessage(RevisionNode.class, "CTL_Action_Opening", event.getFile().getName() + " " + getName()); // NOI18N
        }
        
        protected java.lang.String messageOpened() {
            return ""; // NOI18N
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
            if (event == null) {
                return container.getLog().getAuthor();
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
            if (event == null) {
                return DateFormat.getDateTimeInstance().format(container.getLog().getDate());
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
            if (event == null) {
                return container.getLog().getMessage();
            } else {
                return ""; // NOI18N
            }
        }
    }

    private class RollbackAction extends AbstractAction {

        public RollbackAction() {
            putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackTo", // NOI18N
                    event.getLogInfoHeader().getLog().getRevision().getNumber()));
        }

        public void actionPerformed(ActionEvent e) {
            SummaryView.rollback(event);
        }
    }

    private class RevertModificationsAction extends AbstractAction {

        public RevertModificationsAction() {
            putValue(Action.NAME, NbBundle.getMessage(RevisionNode.class, "CTL_Action_RollbackChange")); // NOI18N
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
            if (event != null) {
                SummaryView.revertModifications(event);
            } else {
                SummaryView.revertModifications(container);
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
