/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.sql.loader;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private transient JSplitPane splitter;
    private transient SQLResultPanel resultComponent;

    private transient SQLExecutionImpl sqlExecution;

    private transient Lookup originalLookup;

    private transient InstanceContent instanceContent = new InstanceContent();
    private transient Lookup ourLookup = new AbstractLookup(instanceContent);

    private transient SQLCloneableEditorLookup resultingLookup;

    public SQLCloneableEditor() {
        super(null);
        putClientProperty("oldInitialize", Boolean.TRUE); // NOI18N
    }

    public SQLCloneableEditor(SQLEditorSupport support) {
        super(support);
        putClientProperty("oldInitialize", Boolean.TRUE); // NOI18N
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
                Logger.getLogger("global").log(Level.INFO, null, e);
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
                Logger.getLogger("global").log(Level.INFO, null, e);
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
                Logger.getLogger("global").log(Level.INFO, null, e);
                return ""; // NOI18N
            }
        }
    }
}
