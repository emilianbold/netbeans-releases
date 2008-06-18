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
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

/*
 * DataViewSourceMultiViewElement.java
 *
 * Created on October 13, 2005, 2:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.db.dataview.editor;

import org.netbeans.modules.db.dataview.editor.ResultSetTabbedPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import javax.swing.JToolBar;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.actions.FileSystemAction;
import org.openide.awt.TabbedPaneFactory;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class DataViewSourceMultiViewElement extends CloneableEditor implements MultiViewElement {

    static final long serialVersionUID = 4403502726950453345L;
    private transient JComponent toolBar;
    private transient MultiViewElementCallback multiViewObserver;
    private DataViewDataObject dataObject;
    private transient JComboBox cb;
    private transient DatabaseConnection selectedConnection = null;
    private final String URL = "org/netbeans/modules/db/dataview/editor/images/runCollaboration.png";
    private final Icon ICON_TEST_RUN = new ImageIcon(org.openide.util.Utilities.loadImage(URL, true)); // NOI18N

    private transient ResultSetTabbedPane splitter;

    public DataViewSourceMultiViewElement() {
        super();
    }

    public DataViewSourceMultiViewElement(DataViewDataObject dataObject) {
        super(dataObject.getDataViewEditorSupport());
        this.dataObject = dataObject;
        initialize();
        setActivatedNodes(new Node[]{dataObject.getNodeDelegate()});
    }

    public void addResultSet(JPanel dvPanel) {
        if (splitter == null) {
            createResultComponent();
        }
        splitter.addPanel(dvPanel);
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

        JTabbedPane tabPane = TabbedPaneFactory.createCloseButtonTabbedPane();
        splitter = new ResultSetTabbedPane(JSplitPane.VERTICAL_SPLIT, editor, tabPane);
        splitter.setBorder(null);

        container.add(splitter);
        splitter.setDividerLocation(200);
        splitter.setDividerSize(7);

        container.invalidate();
        container.validate();
        container.repaint();

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
        Component[] components = ((JComponent) parent).getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component instanceof JPanel && DataViewEditorSupport.EDITOR_CONTAINER.equals(component.getName())) {
                return (JPanel) component;
            }
            JPanel container = findContainer(component);
            if (container != null) {
                return container;
            }
        }
        return null;
    }

    private void initialize() {
        /**
         * only thing which works to make the XmlNav show for this MVElement see
         * (http://www.netbeans.org/issues/show_bug.cgi?id=67257)
         */
        associateLookup(new ProxyLookup(new Lookup[]{
                    Lookups.fixed(new Object[]{
                        getActionMap(),
                        dataObject,
                        dataObject.getNodeDelegate()
                    })
                }));
    }

    public JComponent getToolbarRepresentation() {
        if (null != toolBar) {
            return toolBar;
        }

        Document doc = getEditorPane().getDocument();
       if (doc instanceof NbDocument.CustomToolbar) {
           if (toolBar == null) {
               toolBar = ((NbDocument.CustomToolbar) doc).createToolbar(getEditorPane());
               if(toolBar != null) {
                toolBar.removeAll();
               } else {
                   toolBar = new JToolBar();
               }
           }
       }

        JButton btn = new JButton(ICON_TEST_RUN);

        cb = new JComboBox();
        toolBar.add(btn);
        toolBar.add(cb);

        DatabaseExplorerUIs.connect(cb, ConnectionManager.getDefault());        
        cb.setSelectedIndex(0);
        selectedConnection = (DatabaseConnection) cb.getSelectedItem();

        cb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object sc = cb.getSelectedItem();
                if( sc instanceof DatabaseConnection) {
                    selectedConnection = (DatabaseConnection)sc;
                }
            }
        });

        final ExecuteQuery action = new ExecuteQuery(this);
        btn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(e);
            }
        });
        return toolBar;
    }

    public JComponent getVisualRepresentation() {
        return this;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
        DataViewEditorSupport editor = dataObject.getDataViewEditorSupport();
        editor.setTopComponent(callback.getTopComponent());
    }

    @Override
    public void requestVisible() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }

    }

    @Override
    public void requestActive() {
        if (multiViewObserver != null) {
            multiViewObserver.requestActive();
        } else {
            super.requestActive();
        }

    }

    @Override
    protected boolean closeLast() {
        return true;
    }

    @Override
    public org.openide.awt.UndoRedo getUndoRedo() {
        return super.getUndoRedo();
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!DataViewEditorSupport.isLastView(multiViewObserver.getTopComponent())) {
            return CloseOperationState.STATE_OK;
        }

        return MultiViewFactory.createUnsafeCloseState(
                "ID_TEXT_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ONLY_OPENED;
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        setActivatedNodes(new Node[0]);
        setActivatedNodes(new Node[]{dataObject.getNodeDelegate()});
        DataObjectProvider.activeDataObject = dataObject;
    }

    @Override
    public void componentClosed() {
        super.canClose(null, true);
        super.componentClosed();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        DataObjectProvider.activeDataObject = dataObject;
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        DataObjectProvider.activeDataObject = dataObject;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(dataObject);
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object firstObject = in.readObject();
        if (firstObject instanceof DataViewDataObject) {
            dataObject = (DataViewDataObject) firstObject;
        }

    }

    @Override
    public Action[] getActions() {
        ArrayList<Action> actionsList = new ArrayList<Action>();
        for (Action action : super.getActions()) {
            //FileSystemAction gets added from addFromLayers().commenting this will make Local History option appear twice
            if (!(action instanceof FileSystemAction)) {
                actionsList.add(action);
            }
        }
        //actionsList.add(SystemAction.get(ExecuteQuery.class));
        actionsList.add(addFromLayers());
        Action[] actions = new Action[actionsList.size()];
        actionsList.toArray(actions);
        return actions;
    }

    private Action addFromLayers() {
        Action action = null;
        Lookup look = Lookups.forPath("Projects/Actions");
        for (Object next : look.lookupAll(Object.class)) {
            if (next instanceof Action) {
                action = (Action) next;
            } else if (next instanceof JSeparator) {
                action = null;
            }
        }
        return action;
    }

    public DatabaseConnection getSelectedConnection() {
        return selectedConnection;
    }
}
