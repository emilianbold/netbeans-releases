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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.multiview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.schema.SchemaEditorSupport;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.basic.SchemaColumnsCategory;
import org.netbeans.modules.xml.schema.ui.basic.SchemaSettings;
import org.netbeans.modules.xml.schema.ui.basic.SchemaSettings.ViewMode;
import org.netbeans.modules.xml.schema.ui.basic.SchemaTreeCategory;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.validation.ValidateAction;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.category.Category;
import org.netbeans.modules.xml.xam.ui.category.CategoryPane;
import org.netbeans.modules.xml.xam.ui.category.DefaultCategoryPane;
import org.netbeans.modules.xml.search.api.SearchManager;
import org.netbeans.modules.xml.search.api.SearchManagerAccess;
import org.netbeans.modules.xml.xam.ui.multiview.ActivatedNodesMediator;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.explorer.ExplorerManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jeri Lockhart
 * @author Todd Fast, todd.fast@sun.com
 * @author Nathan Fiedler
 */
public class SchemaColumnViewMultiViewElement extends TopComponent
        implements MultiViewElement, PropertyChangeListener,
        ExplorerManager.Provider {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Used in recovery from malformed model. */
    private static final String EMPTY_DOC =
            "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\"/>";
    private SchemaDataObject schemaDataObject;
    private SchemaModel schemaModel;
    private String errorMessage;
    private transient MultiViewElementCallback multiViewCallback;
    private CategoryPane categoryPane;
    private transient JToolBar toolbar;
    private transient javax.swing.JLabel errorLabel = new javax.swing.JLabel();
    private ExplorerManager manager;

    /**
     * Nullary constructor for deserialization.
     */
    public SchemaColumnViewMultiViewElement() {
    }

    public SchemaColumnViewMultiViewElement(SchemaDataObject schemaDataObject) {
        super();
        this.schemaDataObject = schemaDataObject;
        try {
            initialize();
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,
                    NbBundle.getMessage(SchemaColumnViewMultiViewElement.class,
                    "LBL_ColView_not_created"));
        }
    }

    private void initialize() throws IOException {
        SchemaEditorSupport editor = schemaDataObject.getSchemaEditorSupport();
    manager = new ExplorerManager();
        // Install our own actions.
        CallbackSystemAction globalFindAction =
                (CallbackSystemAction) SystemAction.get(FindAction.class);
        Object mapKey = globalFindAction.getActionMapKey();
    ActionMap map = getActionMap();
        map.put(mapKey, new ColumnViewFindAction());
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, false));

        // Define the keyboard shortcuts for the actions.
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = (KeyStroke) globalFindAction.getValue(Action.ACCELERATOR_KEY);
        if (key == null) {
            key = KeyStroke.getKeyStroke("control F");
        }
        keys.put(key, mapKey);

        ShowCookie showCookie = new ShowCookie() {
            
            public void show(ResultItem resultItem) {
                final Component component = resultItem.getComponents();
                if (categoryPane != null && component instanceof SchemaComponent) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            categoryPane.getCategory().showComponent(
                                    (SchemaComponent) component);
                        }
                    });
                }
            }            
        };
        Node delegate = schemaDataObject.getNodeDelegate();
        ActivatedNodesMediator nodesMediator =
                new ActivatedNodesMediator(delegate);
        nodesMediator.setExplorerManager(this);
        CookieProxyLookup cpl = new CookieProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                // Need ActionMap in lookup so our actions are used.
                map,
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                schemaDataObject,
                // The Show Cookie in lookup to show schema component
                showCookie,
            }),
            nodesMediator.getLookup(),
            // The Node delegate Lookup must be the last one in the list
            // for the CookieProxyLookup to work properly.
            delegate.getLookup(),
        }, delegate);
        associateLookup(cpl);
        addPropertyChangeListener("activatedNodes", nodesMediator);
        addPropertyChangeListener("activatedNodes", cpl);
        setLayout(new BorderLayout());
        initUI(true);
    }

    public ExplorerManager getExplorerManager() {
    return manager;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (!SchemaModel.STATE_PROPERTY.equals(property)) {
            return;
        }
        State newState = (State)evt.getNewValue();
        if (newState == SchemaModel.State.VALID) {
            errorMessage = null;
            recreateUI();
            return;
        }

        //model is broken
        if (errorMessage == null) {
            errorMessage = NbBundle.getMessage(
                    SchemaColumnViewMultiViewElement.class,
                    "MSG_NotWellformedSchema");
        }
        setActivatedNodes(new Node[] {schemaDataObject.getNodeDelegate()});
        emptyUI(errorMessage);
    }

    public SchemaDataObject getSchemaDataObject() {
        return schemaDataObject;
    }

    private SchemaModel getSchemaModel() {
        try {
            if(schemaModel != null)
                return schemaModel;
            
            SchemaEditorSupport editor = getSchemaDataObject().getSchemaEditorSupport();
            schemaModel = editor.getModel();
            if (schemaModel != null) {
                PropertyChangeListener pcl = WeakListeners.
                        create(PropertyChangeListener.class, this, schemaModel);
                schemaModel.addPropertyChangeListener(pcl);
            }
        } catch (IOException io) {
            errorMessage = io.getMessage();
        }
        
        return schemaModel;
    }

    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewCallback = callback;
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned designer, closing is OK
        if (!SchemaMultiViewSupport.isLastView(multiViewCallback.getTopComponent())) {
            return CloseOperationState.STATE_OK;
        }
        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_SCHEMA_COLUMNVIEW_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }

    /**
     * Indicates if the schema model is in a valid state.
     *
     * @return  true if schema is valid, false otherwise.
     */
    private boolean isSchemaValid() {
        SchemaEditorSupport editor = schemaDataObject.getSchemaEditorSupport();
        try {
            SchemaModel schemaModel = editor.getModel();
            if (schemaModel != null &&
                    schemaModel.getState() == SchemaModel.State.VALID) {
                return true;
            }
        } catch (IOException io) {
            // Fall through and return false.
        }
        return false;
    }

    /**
     * Initializes the UI. Here it checks for the state of the underlying
     * schema model. If valid, draws the UI, else empties the UI with proper
     * error message.
     */
    private void initUI() {
        initUI(false);
    }

    private void initUI(boolean reloadOnModelInvalid) {
        SchemaModel model = getSchemaModel();
        if (model != null && model.getState() == SchemaModel.State.VALID) {
            recreateUI();
            return;
        }

        if (model != null && reloadOnModelInvalid) {
            forceResetDocument(model);
            if (model.getState() == SchemaModel.State.VALID) {
                recreateUI();
                return;
            }
        }

        // If it comes here, either the schema is not well-formed or invalid.
        if (errorMessage == null) {
            errorMessage = NbBundle.getMessage(
                    SchemaColumnViewMultiViewElement.class,
                    "MSG_NotWellformedSchema");
        }

        // Empties the UI, with proper error message, when the underlying
        // schema model is in INVALID/NOT-WELLFORMED state.
        emptyUI(errorMessage);
    }
    
    /**
     * Creates the UI. Creates the abeDesigner only if it was null
     * or the underlying documnet was found modifed externally.
     */
    private void recreateUI() {
        removeAll();
        if (categoryPane == null) {
            categoryPane = new DefaultCategoryPane();
            SchemaModel model = getSchemaModel();
            Lookup lookup = getLookup();
            Category columns = new SchemaColumnsCategory(model, lookup);
            categoryPane.addCategory(columns);
            Category tree = new SchemaTreeCategory(model, lookup);
            categoryPane.addCategory(tree);
            // Set the default view according to the persisted setting.
            ViewMode mode = SchemaSettings.getDefault().getViewMode();
            switch (mode) {
                case COLUMN:
                    categoryPane.setCategory(columns);
                    break;
                case TREE:
                    categoryPane.setCategory(tree);
                    break;
            }
        }
        // Add the schema category pane as our primary interface.
        add(categoryPane.getComponent(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    /**
     * Empties the UI, with proper error message, when the underlying
     * schema model is in INVALID/NOT-WELLFORMED state.
     */
    private void emptyUI(String errorMessage) {
        removeAll();
        errorLabel.setText("<" + errorMessage + ">");
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        errorLabel.setBackground(usualWindowBkg != null ? usualWindowBkg :
            Color.white);
        errorLabel.setOpaque(true);
        add(errorLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Adds the undo/redo manager to the schema model as an undoable
     * edit listener, so it receives the edits onto the queue.
     */
    private void addUndoManager() {
        SchemaModel model = getSchemaModel();
        if (model != null) {
            SchemaEditorSupport editor = schemaDataObject.getSchemaEditorSupport();
            QuietUndoManager undo = editor.getUndoManager();
            // Ensure the listener is not added twice.
            model.removeUndoableEditListener(undo);
            model.addUndoableEditListener(undo);
            // Ensure the model is sync'd when undo/redo is invoked,
            // otherwise the edits are added to the queue and eventually
            // cause exceptions.
            undo.setModel(model);
            AXIModel aModel = AXIModelFactory.getDefault().getModel(model);
            undo.removeWrapperModel(aModel);
        }
    }
    
    public void componentActivated() {
        super.componentActivated();
        addUndoManager();
        ExplorerUtils.activateActions(manager, true);
    }

    public void componentDeactivated() {
        super.componentDeactivated();
    ExplorerUtils.activateActions(manager, false);
    }

    public void componentOpened() {
        super.componentOpened();
    }

    public void componentClosed() {
        super.componentClosed();
    }

    public void componentShowing() {
        super.componentShowing();
        initUI();
        addUndoManager();
        if (categoryPane != null) {
            Category cat = categoryPane.getCategory();
            if (cat != null) {
                cat.componentShown();
            }
        }
    }

    public void componentHidden() {
        super.componentHidden();
        if (categoryPane != null) {
            Category cat = categoryPane.getCategory();
            if (cat != null) {
                cat.componentHidden();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void requestFocus() {
        super.requestFocus();
        // For Help to work properly, need to take focus.
        if (categoryPane != null) {
            categoryPane.getComponent().requestFocus();
        }
    }

    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow() {
        boolean retVal = super.requestFocusInWindow();
        // For Help to work properly, need to take focus.
        if (categoryPane != null) {
            return categoryPane.getComponent().requestFocusInWindow();
        }
        return retVal;
    }
    
    public JComponent getToolbarRepresentation() {
        // This is called every time user switches between elements.
        if (toolbar == null) {
            try {
                SchemaModel model = schemaDataObject
                        .getSchemaEditorSupport().getModel();
                if (model != null && model.getState() == SchemaModel.State.VALID) {
                    toolbar = new JToolBar();
                    toolbar.setFloatable(false);
                    if (categoryPane != null) {
                        toolbar.addSeparator();
                        categoryPane.populateToolbar(toolbar);
                    }
                    // vlv: search
                    toolbar.addSeparator();
                    SearchManager manager = SearchManagerAccess.getManager();

                    if (manager != null) {
                      toolbar.add(manager.getSearchAction());
                    }
                    toolbar.addSeparator();
                    JButton validateButton = toolbar.add(new ValidateAction(model));
                    validateButton.getAccessibleContext().setAccessibleName(""+
                            validateButton.getAction().getValue(ValidateAction.NAME));
                }
            } catch (IOException ioe) {
                // wait until the model is valid
            }
        }
        return toolbar;
    }

    public UndoRedo getUndoRedo() {
    return schemaDataObject.getSchemaEditorSupport().getUndoManager();
    }

    public JComponent getVisualRepresentation() {
        return this;
    }
    
    public HelpCtx getHelpCtx() {
    return new HelpCtx(SchemaColumnViewMultiViewDesc.class);
    }

    private class ColumnViewFindAction extends AbstractAction {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance of ColumnViewFindAction.
         */
        public ColumnViewFindAction() {
        }

        public void actionPerformed(ActionEvent event) {
            SchemaColumnViewMultiViewElement parent =
                    SchemaColumnViewMultiViewElement.this;
            if (parent.categoryPane != null) {
                CategoryPane pane = parent.categoryPane;
                pane.getSearchComponent().showComponent();
            }
        }
    }

    /**
     * Force the model to sync with a simple document, then sync again with
     * the original document. This is used to reconstruct the model in the
     * event that it is in an invalid state.
     *
     * @param  model  the model to be reset.
     */
    private void forceResetDocument(SchemaModel model) {
        SchemaDataObject dobj = getSchemaDataObject();
        SchemaEditorSupport editor = dobj.getSchemaEditorSupport();
        StyledDocument doc = editor.getDocument();
        QuietUndoManager um = editor.getUndoManager();
        // Must ignore the edits that are about to occur.
        model.removeUndoableEditListener(um);
        // Changing the document causes the editor support to end up
        // creating another MVE, so avoid that altogether by ignoring
        // the call to updateTitles().
        editor.ignoreUpdateTitles(true);
        try {
            // Reset the model state by forcing it to sync again.
            String saved = doc.getText(0, doc.getLength());
            doc.remove(0, doc.getLength());
            doc.insertString(0, EMPTY_DOC, null);
            model.sync();
            doc.remove(0, doc.getLength());
            doc.insertString(0, saved, null);
            model.sync();
            dobj.setModified(false);
        } catch(BadLocationException e) {
            Logger.getLogger(SchemaColumnViewMultiViewElement.class.getName()).log(
                    Level.FINE, "forceResetDocument", e);
        } catch(IOException e) {
            Logger.getLogger(SchemaColumnViewMultiViewElement.class.getName()).log(
                    Level.FINE, "forceResetDocument", e);
        } finally {
            editor.ignoreUpdateTitles(false);
            model.addUndoableEditListener(um);
        }
    }
}
