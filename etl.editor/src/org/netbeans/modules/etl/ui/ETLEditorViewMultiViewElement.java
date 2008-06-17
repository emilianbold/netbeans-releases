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
 * Created on May 19, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.etl.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.java.hulp.i18n.Logger;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.model.ETLDefinition;
import org.netbeans.modules.etl.ui.palette.PaletteSupport;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.spi.palette.PaletteController;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableTopComponent;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ETLEditorViewMultiViewElement extends CloneableTopComponent
        implements MultiViewElement, Lookup.Provider, Externalizable {

    private static final long serialVersionUID = -655912409997381426L;
    private ETLDataObject dataObject = null;
    private ETLCollaborationTopPanel topPanel;
    private ETLEditorSupport mEditorSupport = null;
    private transient InstanceContent nodesHack;
    private transient javax.swing.JLabel errorLabel = new javax.swing.JLabel();
    private static transient final Logger mLogger = Logger.getLogger(ETLEditorViewMultiViewElement.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
    public ETLEditorViewMultiViewElement() {
        super();
    }

    public ETLEditorViewMultiViewElement(ETLDataObject dObj) {
        super();
        this.dataObject = dObj;
        try {
            initialize();
        } catch (Exception ex) {
            String nbBundle1 = mLoc.t("BUND177: Error in creating eTL Editor view");
            ErrorManager.getDefault().log(ErrorManager.ERROR,
                 nbBundle1.substring(15) + ex.getMessage());
        }
    }

    private void initialize() throws Exception {
        try {
            mEditorSupport = dataObject.getETLEditorSupport();
            setLayout(new BorderLayout());
            topPanel = dataObject.getETLEditorTopPanel();
            add(BorderLayout.CENTER, topPanel);
            initializeLookup();
        } catch (Exception ex) {
            throw ex;
        }
    }

    private void initializeLookup() throws IOException {
        associateLookup(createAssociateLookup());
        addPropertyChangeListener(new PropertyChangeListener() {

            /**
             * TODO: may not be needed at some point when parenting
             * MultiViewTopComponent delegates properly to its peer's
             * activatedNodes.
             *
             * see http://www.netbeans.org/issues/show_bug.cgi?id=67257
             *
             * note: TopComponent.setActivatedNodes is final
             */
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("activatedNodes")) {
                    nodesHack.set(Arrays.asList(getActivatedNodes()), null);
                }
            }
        });
        setActivatedNodes(new Node[]{getETLDataObject().getNodeDelegate()});
    }

    private Lookup createAssociateLookup() throws IOException {
        //
        // see http://www.netbeans.org/issues/show_bug.cgi?id=67257
        //
        final PaletteController controller = createPalette();
        nodesHack = new InstanceContent();
        nodesHack.add(controller);
        /*controller.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if( PaletteController.PROP_SELECTED_ITEM.equals( evt.getPropertyName() ) ) {
                    Lookup selItem = controller.getSelectedItem();
                    if( null != selItem ) {
                        ActiveEditorDrop selNode = (ActiveEditorDrop)selItem.lookup( ActiveEditorDrop.class );
                        if(null != selNode){
                            StatusDisplayer.getDefault().setStatusText("Selected "+selNode);
                        }
                    }
                }
            }
            
        });
        setDropTarget(new DropTarget(this,new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent dtde) {            
            }

            public void dragOver(DropTargetDragEvent dtde) {                
            }

            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            public void dragExit(DropTargetEvent dte) {
            }

            public void drop(DropTargetDropEvent dtde) {
              NotifyDescriptor d =new NotifyDescriptor.Message("Drop",NotifyDescriptor.INFORMATION_MESSAGE);
              DialogDisplayer.getDefault().notify(d);
            }
        }));*/
        return new ProxyLookup(new Lookup[]{
            //
            // other than nodesHack what else do we need in the associated
            // lookup?  I think that XmlNavigator needs DataObject
            //
            getETLDataObject().getLookup(), // this lookup contain objects that are used in OM clients
            Lookups.singleton(this),
            new AbstractLookup(nodesHack),
            Lookups.fixed(new Object[]{controller}),});
    }

    private PaletteController createPalette() throws IOException {
        PaletteController controller = PaletteSupport.createPalette(topPanel);
        return controller;
    }

    private ETLDataObject getETLDataObject() {
        return dataObject;
    }

    /**
     * Overwrite when you want to change default persistence type. Default
     * persistence type is PERSISTENCE_ALWAYS.
     * Return value should be constant over a given TC's lifetime.
     * @return one of P_X constants
     * @since 4.20
     */
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ONLY_OPENED;//was PERSISTENCE_NEVER
    }

    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        ETLEditorSupport editor = dataObject.getETLEditorSupport();
        editor.setTopComponent(callback.getTopComponent());
    }

    public CloseOperationState canCloseElement() {
        if ((dataObject != null) && (dataObject.isModified())) {
            return MultiViewFactory.createUnsafeCloseState("Data object modified", null, null);
        }
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        DataObjectProvider.activeDataObject = dataObject;
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        if (getETLDataObject().getETLEditorSupport().isFirstTime) {
            try {
                getETLDataObject().getETLEditorSupport().syncModel();
                SQLObjectUtil.createMissingModelTablesInDB(getETLDataObject());
            } catch (Exception ex) {
                ex.printStackTrace();
                StatusDisplayer.getDefault().setStatusText(ex.getMessage());
            }
        }
        getETLDataObject().createNodeDelegate();
        DataObjectProvider.activeDataObject = dataObject;
        /*GraphView graphView = (GraphView) this.topPanel.getGraphView();
        if (null != graphView) {
            graphView.setObserved(graphView);
        }*/
    }

    @Override
    public void componentDeactivated() {
        SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
        if (cookie != null) {
            getETLDataObject().getETLEditorSupport().synchDocument();
        }
        super.componentDeactivated();
    }

    @Override
    public void componentShowing() {

        super.componentShowing();
        ETLEditorSupport editor = dataObject.getETLEditorSupport();
        UndoRedo.Manager undoRedo = editor.getUndoManager();
        Document document = editor.getDocument();
        if (document != null) {
            document.removeUndoableEditListener(undoRedo);
        }
        checkModelState();
        DataObjectProvider.activeDataObject = dataObject;
    }

    private void checkModelState() {
        ETLEditorSupport editor = getETLDataObject().getETLEditorSupport();
        ETLDataObject dObj = (ETLDataObject) editor.getDataObject();
        ETLDefinition model = null;
        String errorMessage = null;

        try {
            model = dObj.getETLDefinition();
            if (model != null) {
                recreateUI(model);
                return;
            }
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
            ex.printStackTrace();
        }

        //if it comes here, either the schema is not well-formed or invalid
        if (model == null) {
            if (errorMessage == null) {
                String nbBundle2 = mLoc.t("BUND178: The etl is not well-formed");
                errorMessage = nbBundle2.substring(15);
            }
        }

        emptyUI(errorMessage);
    }

    private void emptyUI(String errorMessage) {
        this.removeAll();
        errorLabel.setText("<" + errorMessage + ">");
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        errorLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        errorLabel.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        errorLabel.setOpaque(true);
        add(errorLabel, BorderLayout.CENTER);
    }

    private void recreateUI(ETLDefinition model) {
        this.removeAll();
        // Add the schema category pane as our primary interface.
        add(topPanel, BorderLayout.CENTER);
    }

    public javax.swing.JComponent getToolbarRepresentation() {
        return topPanel.createToolbar();
    }

    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }

    public CloneableTopComponent getComponent() {
        return this;
    }

    /** Get the undo/redo support for this component.
     * The default implementation returns a dummy support that cannot
     * undo anything.
     *
     * @return undoable edit for this component
     */
    @Override
    public UndoRedo getUndoRedo() {
        return new TreeEditorUndoRedo();
    }

    public void cleanUp() {
    }

    public class TreeEditorUndoRedo implements UndoRedo {

        public void addChangeListener(ChangeListener l) {
        }

        public boolean canRedo() {
            return mEditorSupport.getUndoManager().canRedo();
        }

        public boolean canUndo() {
            return mEditorSupport.getUndoManager().canUndo();
        }

        public String getRedoPresentationName() {
            if (mEditorSupport != null) {
                return mEditorSupport.getUndoManager().getRedoPresentationName();
            }
            return "Redo_ETLTreeEditor";
        }

        public String getUndoPresentationName() {
            if (mEditorSupport != null) {
                return mEditorSupport.getUndoManager().getUndoPresentationName();
            }
            return "Undo_ETLTreeEditor";
        }

        public void redo() throws CannotRedoException {
            mEditorSupport.getUndoManager().redo();
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public void undo() throws CannotUndoException {
            mEditorSupport.getUndoManager().undo();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(dataObject);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object firstObject = in.readObject();
        if (firstObject instanceof ETLDataObject) {
            dataObject = (ETLDataObject) in.readObject();
        }
    }

    @Override
    public Action[] getActions() {
        ArrayList<Action> actionsList = new ArrayList<Action>();
        for (Action action : super.getActions()) {            
            actionsList.add(action);
        }
        actionsList.addAll(Utilities.actionsForPath("Projects/Actions"));
        Action[] actions = new Action[actionsList.size()];
        actionsList.toArray(actions);
        return actions;
    }

}
