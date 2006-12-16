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

package org.netbeans.modules.db.sql.loader;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.netbeans.modules.db.sql.execute.ui.SQLResultPanel;
import org.openide.ErrorManager;
import org.openide.text.CloneableEditor;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 * Cloneable editor for SQL. It it was opened as a console,
 * it saves its document when its is deactivated or serialized. Also has
 * a SQLExecution implementation in its lookup.
 *
 * @author Andrei Badea
 */
public class SQLCloneableEditor extends CloneableEditor {

    private transient JPanel container;
    private transient JSplitPane splitter;
    private transient SQLResultPanel resultComponent;

    private transient SQLExecutionImpl sqlExecution;

    private transient Lookup originalLookup;

    private transient InstanceContent instanceContent = new InstanceContent();
    private transient Lookup ourLookup = new AbstractLookup(instanceContent);

    private transient SQLCloneableEditorLookup resultingLookup;

    public SQLCloneableEditor() {
        super(null);
    }

    public SQLCloneableEditor(SQLEditorSupport support) {
        super(support);
        initialize();
    }

    public boolean hasResultComponent() {
        return resultComponent != null;
    }

    public SQLResultPanel getResultComponent() {
        assert SwingUtilities.isEventDispatchThread();
        if (resultComponent == null) {
            createResultComponent();
        }
        return resultComponent;
    }

    private void createResultComponent() {
        JPanel container = findContainer(this);
        if (container == null) {
            // the editor has just been deserialized and has not been initialized yet
            // thus CES.wrapEditorComponent() has not been called yet
            return;
        }

        Component editor = container.getComponent(0);
        container.removeAll();

        resultComponent = new SQLResultPanel();
        splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor, resultComponent);
        splitter.setBorder(null);

        container.add(splitter);
        splitter.setDividerLocation(250);
        splitter.setDividerSize(7);

        container.invalidate();
        container.validate();
        container.repaint();

        // #69642: the parent of the CloneableEditor's ActionMap is
        // the editor pane's ActionMap, therefore the delete action is always returned by the
        // CloneableEditor's ActionMap.get(). This workaround delegates to the editor pane
        // only when the editor pane has the focus.
        getActionMap().setParent(new DelegateActionMap(getActionMap().getParent(), getEditorPane()));

        if (equals(TopComponent.getRegistry().getActivated())) {
            // setting back the focus lost when removing the editor from the CloneableEditor
            requestFocusInWindow();
        }
    }

    /**
     * Finds the container component added by SQLEditorSupport.wrapEditorComponent.
     * Not very nice, but avoids the API change in #69466.
     */
    private JPanel findContainer(Component parent) {
        if (!(parent instanceof JComponent)) {
            return null;
        }
        Component[] components = ((JComponent)parent).getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component instanceof JPanel && SQLEditorSupport.EDITOR_CONTAINER.equals(component.getName())) {
                return (JPanel)component;
            }
            JPanel container = findContainer(component);
            if (container != null) {
                return container;
            }
        }
        return null;
    }

    public synchronized Lookup getLookup() {
        Lookup currentLookup = super.getLookup();
        if (currentLookup != originalLookup) {
            originalLookup = currentLookup;
            if (resultingLookup == null) {
                resultingLookup = new SQLCloneableEditorLookup();
            }
            resultingLookup.updateLookups(new Lookup[] { originalLookup, ourLookup });
        }
        return resultingLookup;
    }

    protected void componentDeactivated() {
        if (sqlEditorSupport().isConsole()) {
            try {
                cloneableEditorSupport().saveDocument();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        super.componentDeactivated();
    }

    protected void componentClosed() {
        sqlExecution.editorClosed();
        super.componentClosed();
    }

    public void writeExternal(java.io.ObjectOutput out) throws IOException {
        if (sqlEditorSupport().isConsole()) {
            try {
                cloneableEditorSupport().saveDocument();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        super.writeExternal(out);
    }

    public void readExternal(java.io.ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        initialize();
    }

    private void initialize() {
        sqlExecution = new SQLExecutionImpl();
        instanceContent.add(sqlExecution);
    }

    private SQLEditorSupport sqlEditorSupport() {
        return (SQLEditorSupport)cloneableEditorSupport();
    }

    private static final class DelegateActionMap extends ActionMap {

        private ActionMap delegate;
        private JEditorPane editorPane;

        public DelegateActionMap(ActionMap delegate, JEditorPane editorPane) {
            this.delegate = delegate;
            this.editorPane = editorPane;
        }

        public void remove(Object key) {

            super.remove(key);
        }

        public javax.swing.Action get(Object key) {
            boolean isEditorPaneFocused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner() == editorPane;
            if (isEditorPaneFocused) {
                return delegate.get(key);
            } else {
                return null;
            }
        }

        public void put(Object key, Action action) {
            delegate.put(key, action);
        }

        public void setParent(ActionMap map) {
            delegate.setParent(map);
        }

        public int size() {
            return delegate.size();
        }

        public Object[] keys() {
            return delegate.keys();
        }

        public ActionMap getParent() {
            return delegate.getParent();
        }

        public void clear() {
            delegate.clear();
        }

        public Object[] allKeys() {
            return delegate.allKeys();
        }
    }

    private static final class SQLCloneableEditorLookup extends ProxyLookup {

        public SQLCloneableEditorLookup() {
            super(new Lookup[0]);
        }

        public void updateLookups(Lookup[] lookups) {
            setLookups(lookups);
        }
    }

    /**
     * Implementation of SQLExecution delegating to the editor's SQLEditorSupport.
     */
    private final class SQLExecutionImpl implements SQLExecution, PropertyChangeListener {

        // we add the property change listeners to our own support instead of
        // the editor's one to ensure the editor does not reference e.g. actions
        // which forgot the remove their listeners. the editor would
        // prevent them from begin GCd (since the editor's life will usually
        // be longer than that of the actions)
        private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

        public SQLExecutionImpl() {
            sqlEditorSupport().addSQLPropertyChangeListener(this);
        }

        private void editorClosed() {
            sqlEditorSupport().removeSQLPropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent event) {
            propChangeSupport.firePropertyChange(event);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propChangeSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propChangeSupport.removePropertyChangeListener(listener);
        }

        public DatabaseConnection getDatabaseConnection() {
            return sqlEditorSupport().getDatabaseConnection();
        }

        public void setDatabaseConnection(DatabaseConnection dbconn) {
            sqlEditorSupport().setDatabaseConnection(dbconn);
        }

        public void execute() {
            String text = Mutex.EVENT.readAccess(new Mutex.Action<String>() {
                public String run() {
                    return getText(getEditorPane());
                }
            });
            sqlEditorSupport().execute(text, 0, text.length());
        }

        public void executeSelection() {
            final int[] offsets = new int[2];
            String text = Mutex.EVENT.readAccess(new Mutex.Action<String>() {
                public String run() {
                    JEditorPane editorPane = getEditorPane();
                    int startOffset = editorPane.getSelectionStart();
                    int endOffset = editorPane.getSelectionEnd();
                    if (startOffset == endOffset) {
                        // there is no selection, execute the statement under 
                        // the caret
                        offsets[0] = offsets[1] = editorPane.getCaretPosition();
                    } else {
                        offsets[0] = startOffset;
                        offsets[1] = endOffset;
                    }
                    return getText(editorPane);
                }
            });
            sqlEditorSupport().execute(text, offsets[0], offsets[1]);
        }

        public boolean isExecuting() {
            return sqlEditorSupport().isExecuting();
        }

        public boolean isSelection() {
            Boolean result = Mutex.EVENT.readAccess(new Mutex.Action<Boolean>() {
                public Boolean run() {
                    JEditorPane editorPane = getEditorPane();
                    return Boolean.valueOf(editorPane.getSelectionStart() < editorPane.getSelectionEnd());
                }
            });
            return result.booleanValue();
        }
        
        public String toString() {
            return "SQLExecution[support=" + sqlEditorSupport().messageName()  + ", dbconn=" + sqlEditorSupport().getDatabaseConnection() + "]"; // NOI18N
        }
        
        private String getText(JEditorPane editorPane) {
            // issue 75529: must not use the simpler JEditorPane.getText() 
            // since we want to obtain the text from the document, which has
            // line ends normalized to '\n'
            Document doc = editorPane.getDocument();
            try {
                return doc.getText(0, doc.getLength());
            } catch (BadLocationException e) {
                // should not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return ""; // NOI18N
            }
        }
    }
}
