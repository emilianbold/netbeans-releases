/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.beans.*;

import org.netbeans.core.spi.multiview.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.ExplorerManager;

import org.netbeans.modules.form.palette.CPManager;
import org.netbeans.modules.form.wizard.ConnectionWizard;

/**
 * This is a TopComponent subclass holding the form designer. It consist of two
 * layers - HandleLayer (responsible for interaction with user) and
 * ComponentLayer (presenting the components, not accessible to the user).
 *
 * FormDesigner
 *  +- FormToolBar
 *  +- JScrollPane
 *      +- JLayeredPane
 *          +- HandleLayer
 *          +- ComponentLayer
 *
 * @author Tran Duc Trung, Tomas Pavek, Josef Kozak
 */

public class FormDesigner extends TopComponent implements MultiViewElement
{
    static final String PROP_DESIGNER_SIZE = "designerSize"; // NOI18N

    private JLayeredPane layeredPane;

    private ComponentLayer componentLayer;
    private HandleLayer handleLayer;

    private FormToolBar formToolBar;

    private InPlaceEditLayer textEditLayer;
    private FormProperty editedProperty;

    private RADVisualComponent topDesignComponent;

    private FormModel formModel;
    private FormModelListener formModelListener;

    private FormEditorSupport formEditorSupport;

    private VisualReplicator replicator = new VisualReplicator(
        null,
        new Class[] { Window.class,
                      java.applet.Applet.class,
                      MenuComponent.class },
        VisualReplicator.ATTACH_FAKE_PEERS | VisualReplicator.DISABLE_FOCUSING);

    private final ArrayList selectedComponents = new ArrayList();

    private int designerMode;
    static final int MODE_SELECT = 0;
    static final int MODE_CONNECT = 1;
    static final int MODE_ADD = 2;
    
    private boolean initialized = false;

    private RADComponent connectionSource;
    private RADComponent connectionTarget;

    MultiViewElementCallback multiViewObserver;

    private ExplorerManager explorerManager;

    /** The icons for FormDesigner */
    private static String iconURL =
        "org/netbeans/modules/form/resources/formDesigner.gif"; // NOI18N

    // ----------
    // constructors and setup
    
    FormDesigner(FormEditorSupport fes) {
        setIcon(Utilities.loadImage(iconURL));
        setLayout(new BorderLayout());
        
        FormLoaderSettings settings = FormLoaderSettings.getInstance();
        Color backgroundColor = settings.getFormDesignerBackgroundColor();
        Color borderColor = settings.getFormDesignerBorderColor();
        
        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        loadingPanel.setBackground(backgroundColor);
        JLabel loadingLbl = new JLabel(FormUtils.getBundleString("LBL_FormLoading")); // NOI18N
        loadingLbl.setOpaque(true);
        loadingLbl.setPreferredSize(new Dimension(410,310));
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingPanel.add(loadingLbl);        
        loadingLbl.setBorder(new CompoundBorder(new LineBorder(borderColor, 5),
            new EmptyBorder(new Insets(6, 6, 6, 6))));
        add(loadingPanel, BorderLayout.CENTER);
        
        formEditorSupport = fes;
        explorerManager = new ExplorerManager();
        
        // add FormDataObject to lookup so it can be obtained from multiview TopComponent
        ActionMap map = ComponentInspector.getInstance().setupActionMap(getActionMap());
        FormDataObject formDataObject = formEditorSupport.getFormDataObject();
        associateLookup(new ProxyLookup(new Lookup[] {
            ExplorerUtils.createLookup(explorerManager, map),
            Lookups.fixed(new Object[] { formDataObject }),
            // should not affect selected nodes, but should provide cookies etc.
            new NoNodeLookup(formDataObject.getNodeDelegate().getLookup())
        }));
        
        formToolBar = new FormToolBar(this);
    }
    
    private void initialize() {
        initialized = true;
        removeAll();

        componentLayer = new ComponentLayer();
        handleLayer = new HandleLayer(this);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.add(componentLayer, new Integer(1000));
        layeredPane.add(handleLayer, new Integer(1001));

        JScrollPane scrollPane = new JScrollPane(layeredPane);
        scrollPane.setBorder(null); // disable border, winsys will handle borders itself
        add(scrollPane, BorderLayout.CENTER);

        explorerManager.setRootContext(formEditorSupport.getFormRootNode());
        addPropertyChangeListener("activatedNodes", new PropertyChangeListener() { // NOI18N
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    explorerManager.setSelectedNodes((Node[])evt.getNewValue());
                } catch (PropertyVetoException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        setModel(formEditorSupport.getFormModel());
        updateWholeDesigner();
        
        // not very nice hack - it's better FormEditorSupport has its
        // listener registered after FormDesigner
        formEditorSupport.reinstallListener();
    }

    // only MultiViewDescriptor is stored, not MultiViewElement
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    void setModel(FormModel m) {
        if (formModel != null) {
            if (formModelListener != null)
                formModel.removeFormModelListener(formModelListener);
            topDesignComponent = null;
        }

        formModel = m;

        if (formModel != null) {
            if (formModelListener == null)
                formModelListener = new FormListener();
            formModel.addFormModelListener(formModelListener);
            formEditorSupport = FormEditorSupport.getFormEditor(formModel);
            resetTopDesignComponent(false);
            handleLayer.setViewOnly(formModel.isReadOnly());
            componentLayer.updateDesignerSize(getStoredDesignerSize());
        }
        else formEditorSupport = null;
    }

    FormModel getModel() {
        return formModel;
    }

    FormEditorSupport getFormEditorSupport() {
        return formEditorSupport;
    }

    HandleLayer getHandleLayer() {
        return handleLayer;
    }

    ComponentLayer getComponentLayer() {
        return componentLayer;
    }

    FormToolBar getFormToolBar() {
        return formToolBar;
    }

    // ------------
    // designer content

    public Object getComponent(RADComponent metacomp) {
        return replicator.getClonedComponent(metacomp);
    }

    public RADComponent getMetaComponent(Object comp) {
        return replicator.getMetaComponent(comp);
    }

    public RADVisualComponent getTopDesignComponent() {
        return topDesignComponent;
    }

    public void setTopDesignComponent(RADVisualComponent component,
                                      boolean update) {
        topDesignComponent = component;
        if (update) {
            setSelectedComponent(topDesignComponent);
            updateWholeDesigner();
        }
    }

    public void resetTopDesignComponent(boolean update) {
        if (formModel.getTopRADComponent() instanceof RADVisualComponent)
            topDesignComponent = (RADVisualComponent)
                                 formModel.getTopRADComponent();
        else topDesignComponent = null;

        if (update) {
            setSelectedComponent(topDesignComponent);
            updateWholeDesigner();
        }
    }

    /** Tests whether top designed container is some parent of given component
     * (whether the component is in the tree under top designed container).
     */
    public boolean isInDesignedTree(RADComponent metacomp) {
        return topDesignComponent != null
               && (topDesignComponent == metacomp
                   || topDesignComponent.isParentComponent(metacomp));
    }

    void updateWholeDesigner() {
        if (formModelListener != null)
            formModelListener.formChanged(null);
    }

    public static Container createFormView(final RADVisualComponent metacomp,
                                           final Class contClass)
        throws Exception
    {
        return (Container) FormLAF.executeWithLookAndFeel(
            new Mutex.ExceptionAction () {
                public Object run() throws Exception {
                    VisualReplicator r =
                        new VisualReplicator(contClass, null, 0);
                    r.setTopMetaComponent(metacomp);
                    return r.createClone();
                }
            }
        );
    }

    // -------
    // designer mode

    void setDesignerMode(int mode) {
        formToolBar.updateDesignerMode(mode);

        if (mode == designerMode)
            return;

        designerMode = mode;

        resetConnection();
        if (mode == MODE_CONNECT)
            clearSelection();
    }

    int getDesignerMode() {
        return designerMode;
    }

    void toggleSelectionMode() {
        setDesignerMode(MODE_SELECT);
        CPManager.getDefault().setSelectedItem(null);
    }

    void toggleConnectionMode() {
        setDesignerMode(MODE_CONNECT);
        CPManager.getDefault().setSelectedItem(null);
    }

    void toggleAddMode() {
        setDesignerMode(MODE_ADD);
        CPManager.getDefault().setSelectedItem(null);
    }

    // -------
    // designer size

    Dimension getStoredDesignerSize() {
        RADComponent metacomp = formModel.getTopRADComponent();
        if (metacomp == null)
            return null;

        return (Dimension) metacomp.getAuxValue(PROP_DESIGNER_SIZE);
    }

    void setStoredDesignerSize(Dimension size) {
        RADComponent metacomp = formModel.getTopRADComponent();
        if (metacomp instanceof RADVisualFormContainer)
            ((RADVisualFormContainer)metacomp).setDesignerSize(size);
        else if (metacomp != null)
            metacomp.setAuxValue(PROP_DESIGNER_SIZE, size);
    }

    // ---------
    // components selection

    java.util.List getSelectedComponents() {
        return selectedComponents;
    }

    boolean isComponentSelected(RADComponent metacomp) {
        return selectedComponents.contains(metacomp);
    }

    void setSelectedComponent(RADComponent metacomp) {
        clearSelectionImpl();
        addComponentToSelectionImpl(metacomp);
        repaintSelection();
        updateComponentInspector();
    }

    void setSelectedComponents(RADComponent[] metacomps) {
        clearSelectionImpl();

        for (int i=0; i < metacomps.length; i++)
            addComponentToSelectionImpl(metacomps[i]);

        repaintSelection();
        updateComponentInspector();
    }

    void setSelectedNode(FormNode node) {
        if (node instanceof RADComponentNode)
            setSelectedComponent(((RADComponentNode)node).getRADComponent());
        else {
            clearSelectionImpl();
            repaintSelection();

            ComponentInspector ci = ComponentInspector.getInstance();
            if (ci.getFocusedForm() != formEditorSupport)
                return;

            Node[] selectedNodes = new Node[] { node };
            try {
                ci.setSelectedNodes(selectedNodes, formEditorSupport);
                // sets also the activated nodes (both for ComponentInspector
                // and FormDesigner)
            }
            catch (java.beans.PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    }

    void addComponentToSelection(RADComponent metacomp) {
        addComponentToSelectionImpl(metacomp);
        repaintSelection();
        updateComponentInspector();
    }

    void addComponentsToSelection(RADComponent[] metacomps) {
        for (int i=0; i < metacomps.length; i++)
            addComponentToSelectionImpl(metacomps[i]);

        repaintSelection();
        updateComponentInspector();
    }

    void removeComponentFromSelection(RADComponent metacomp) {
        removeComponentFromSelectionImpl(metacomp);
        repaintSelection();
        updateComponentInspector();
    }

    public void clearSelection() {
        clearSelectionImpl();
        repaintSelection();
        updateComponentInspector();
    }

    void addComponentToSelectionImpl(RADComponent metacomp) {
        if (metacomp != null) {
            selectedComponents.add(metacomp);
            if (metacomp instanceof RADVisualComponent)
                ensureComponentIsShown((RADVisualComponent)metacomp);
        }
    }

    void removeComponentFromSelectionImpl(RADComponent metacomp) {
        selectedComponents.remove(metacomp);
    }

    void clearSelectionImpl() {
        selectedComponents.clear();
    }

    void repaintSelection() {
        Rectangle r = componentLayer.getDesignerOuterBounds();
        int borderSize = FormLoaderSettings.getInstance().getSelectionBorderSize();
        handleLayer.repaint(0, r.x - borderSize, r.y - borderSize,
                            r.width + 2*borderSize, r.height + 2*borderSize);
    }

    /** Finds out what component follows after currently selected component
     * when TAB (forward true) or Shift+TAB (forward false) is pressed. 
     * @return the next or previous component for selection
     */
    RADVisualComponent getNextVisualComponent(boolean forward) {
        RADVisualComponent currentComp = null;
        int n = selectedComponents.size();
        if (n > 0) {
            if (n > 1)
                return null;
            Object sel = selectedComponents.get(0);
            if (sel instanceof RADVisualComponent)
                currentComp = (RADVisualComponent) sel;
            else return null;
        }

        return getNextVisualComponent(currentComp, forward);
    }

    /** @return the next or prevoius component to component comp
     */
    RADVisualComponent getNextVisualComponent(RADVisualComponent comp,
                                              boolean forward)
    {
        if (comp == null)
            return topDesignComponent;
        if (getComponent(comp) == null)
            return null;

        RADVisualContainer cont;
        RADVisualComponent[] subComps;

        if (forward) {
            // try the first sub-component
            if (comp instanceof RADVisualContainer) {
                subComps = ((RADVisualContainer)comp).getSubComponents();
                if (subComps.length > 0)
                    return subComps[0];
            }

            // try the next component (or the next of the parent then)
            if (comp == topDesignComponent)
                return topDesignComponent;
            cont = comp.getParentContainer();
            if (cont == null)
                return null; // should not happen

            int i = cont.getIndexOf(comp);
            while (i >= 0) {
                subComps = cont.getSubComponents();
                if (i+1 < subComps.length)
                    return subComps[i+1];

                if (cont == topDesignComponent)
                    break;
                comp = cont; // one level up
                cont = comp.getParentContainer();
                if (cont == null)
                    return null; // should not happen
                i = cont.getIndexOf(comp);
            }

            return topDesignComponent;
        }
        else { // backward
            // take the previuos component
            if (comp != topDesignComponent) {
                cont = comp.getParentContainer();
                if (cont == null)
                    return null; // should not happen
                int i = cont.getIndexOf(comp);
                if (i >= 0) { // should be always true
                    if (i == 0) return cont; // the opposite to the 1st forward step

                    subComps = cont.getSubComponents();
                    comp = subComps[i-1];
                }
                else comp = topDesignComponent;
            }

            // find the last subcomponent of it
            do {
                if (comp instanceof RADVisualContainer) {
                    subComps = ((RADVisualContainer)comp).getSubComponents();
                    if (subComps.length > 0) { // one level down
                        comp = subComps[subComps.length-1];
                        continue;
                    }
                }
                break;
            }
            while (true);
            return comp;
        }
    }

    // ---------
    // visibility update

    // synchronizes ComponentInspector with selection in FormDesigner
    // [there is a hardcoded relationship between these two views]
    void updateComponentInspector() {
        ComponentInspector ci = ComponentInspector.getInstance();
        if (ci.getFocusedForm() != formEditorSupport)
            return;

        Node[] selectedNodes = new Node[selectedComponents.size()];
        Iterator iter = selectedComponents.iterator();
        int i = 0;
        while (iter.hasNext()) {
            RADComponent metacomp = (RADComponent) iter.next();
            selectedNodes[i++] = metacomp.getNodeReference();
        }
        try {
            ci.setSelectedNodes(selectedNodes, formEditorSupport);
            // sets also the activated nodes (both for ComponentInspector
            // and FormDesigner)
        }
        catch (java.beans.PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }

    void updateVisualSettings() {
        componentLayer.updateVisualSettings();
        layeredPane.revalidate();
        layeredPane.repaint(); // repaints both HanleLayer and ComponentLayer
    }

    private void ensureComponentIsShown(RADVisualComponent metacomp) {
        Component comp = (Component) getComponent(metacomp);
        RADVisualContainer metacont = metacomp.getParentContainer();

        if (comp == null) { // visual component doesn't exist yet
            if (metacont != null)
                metacont.getLayoutSupport().selectComponent(
                               metacont.getIndexOf(metacomp));
            return;
        }

        if (comp.isShowing())
            return; // component is showing
        if (!isInDesignedTree(metacomp))
            return; // component is not in designer

        Component topComp = (Component) getComponent(topDesignComponent);
        if (topComp == null || !topComp.isShowing())
            return; // designer is not showing

        RADVisualComponent child = metacomp;

        while (metacont != null) {
            Container cont = (Container) getComponent(metacont);
            org.netbeans.modules.form.layoutsupport.LayoutSupportManager
                laysup = metacont.getLayoutSupport();
            Container contDelegate = metacont.getContainerDelegate(cont);

            laysup.selectComponent(child.getComponentIndex());
            laysup.arrangeContainer(cont, contDelegate);

            if (metacont == topDesignComponent || cont.isShowing())
                break;

            child = metacont;
            metacont = metacont.getParentContainer();
        }
    }

    // --------------
    // bean connection

    void connectBean(RADComponent metacomp, boolean showDialog) {
        if (connectionSource == null) {
            connectionSource = metacomp;
            handleLayer.repaint();
        }
        else {
            if (metacomp == connectionSource) {
                if (connectionTarget != null) {
                    resetConnection();
                    toggleSelectionMode();
                }
                return;
            }
            connectionTarget = metacomp;
            handleLayer.repaint();
            if (showDialog) {
                if (connectionTarget != null) 
                    createConnection(connectionSource, connectionTarget);
//                resetConnection();
                toggleSelectionMode();
            }
        }
    }

    public RADComponent getConnectionSource() {
        return connectionSource;
    }

    public RADComponent getConnectionTarget() {
        return connectionTarget;
    }

    public void resetConnection() {
        if (connectionSource != null || connectionTarget != null) {
            connectionSource = null;
            connectionTarget = null;
            handleLayer.repaint();
        }
    }

    private void createConnection(RADComponent source, RADComponent target) {
        ConnectionWizard cw = new ConnectionWizard(formModel, source,target);

        if (cw.show()) {
            final Event event = cw.getSelectedEvent();
            final String eventName = cw.getEventName();
            String bodyText = cw.getGeneratedCode();

            formModel.getFormEvents().attachEvent(event, eventName, bodyText);

            // hack: after all updates, switch to editor
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    formModel.getFormEvents().attachEvent(event, eventName, null);
                }
            });
        }
    }

    // -----------------
    // in-place editing

    public void startInPlaceEditing(RADComponent metacomp) {
        if (formModel.isReadOnly())
            return;
        if (textEditLayer != null && textEditLayer.isVisible())
            return;
        if (!isEditableInPlace(metacomp)) // check for sure
            return;

        Component comp = (Component) getComponent(metacomp);
        if (comp == null) { // component is not visible
            notifyCannotEditInPlace();
            return;
        }

        RADProperty property = metacomp.getBeanProperty("text"); // NOI18N
        if (property == null)
            return; // should not happen

        String editText = null;
        try {
            Object text = property.getRealValue();
            if (!(text instanceof String)) text = ""; // or return?
            editText = (String) text;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
            return;
        }

        editedProperty = property;

        if (textEditLayer == null) {
            textEditLayer = new InPlaceEditLayer();
            textEditLayer.setVisible(false);
            textEditLayer.addFinishListener(new InPlaceEditLayer.FinishListener() {
                public void editingFinished(boolean textChanged) {
                    finishInPlaceEditing(textEditLayer.isTextChanged());
                }
            });
            layeredPane.add(textEditLayer, new Integer(2001));
        }
        try {
            textEditLayer.setEditedComponent(comp, editText);
        }
        catch (IllegalArgumentException ex) {
            notifyCannotEditInPlace();
            return;
        }

        textEditLayer.setVisible(true);
        handleLayer.setVisible(false);
        textEditLayer.requestFocus();
    }

    private void finishInPlaceEditing(boolean applyChanges) {
        if (applyChanges) {
            try {
                editedProperty.setValue(textEditLayer.getEditedText());
            }
            catch (Exception ex) { // should not happen
                ex.printStackTrace();
            }
        }

        textEditLayer.setVisible(false);
        handleLayer.setVisible(true);
        handleLayer.requestFocus();
        editedProperty = null;
    }

    public boolean isEditableInPlace(RADComponent metacomp) {
        Component comp = (Component) getComponent(metacomp);
        if (comp == null)
            return false;

        // don't allow in-place editing if there's some AWT parent (it may
        // cause problems with fake peers on some platforms)
        RADComponent parent = metacomp.getParentComponent();
        while (parent != null) {
            if (!JComponent.class.isAssignableFrom(parent.getBeanClass())
                && !RootPaneContainer.class.isAssignableFrom(
                                        parent.getBeanClass()))
                return false;
            parent = parent.getParentComponent();
        }

        return InPlaceEditLayer.supportsEditingFor(metacomp.getBeanClass(),
                                                   false);
    }

    private void notifyCannotEditInPlace() {
        DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message(
                FormUtils.getBundleString("MSG_ComponentNotShown"), // NOI18N
                NotifyDescriptor.WARNING_MESSAGE));
    }

    // --------
    // methods of TopComponent

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.formeditor"); // NOI18N
    }

    public void componentActivated() {
        if (formModel == null)
            return;

        formEditorSupport.setFormDesigner(this);
        ComponentInspector ci = ComponentInspector.getInstance();
        if (ci.getFocusedForm() != formEditorSupport) {
            ci.focusForm(formEditorSupport);
            if (getDesignerMode() == MODE_CONNECT)
                clearSelection();
            else
                updateComponentInspector();
        }

        ci.attachActions();
        if (textEditLayer == null || !textEditLayer.isVisible())
            handleLayer.requestFocus();
    }

    public void componentDeactivated() {
        if (formModel == null)
            return;

        if (textEditLayer != null && textEditLayer.isVisible())
            textEditLayer.finishEditing(false);

        ComponentInspector.getInstance().detachActions();
        resetConnection();
    }

    public UndoRedo getUndoRedo() {
        UndoRedo ur = formModel != null ? formModel.getUndoRedoManager() : null;
        return ur != null ? ur : super.getUndoRedo();
    }
    
    protected String preferredID() {
        return formEditorSupport.getFormDataObject().getName();
    }

    // ------
    // multiview stuff

    public JComponent getToolbarRepresentation() {
        return getFormToolBar();
    }

    public JComponent getVisualRepresentation() {
        return this;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
        
        // add FormDesigner as a client property so it can be obtained
        // from multiview TopComponent (it is not sufficient to put
        // it into lookup - only content of the lookup of the active
        // element is accessible)
        callback.getTopComponent().putClientProperty("formDesigner", this); // NOI18N

        // needed for deserialization...
        if (formEditorSupport != null) {
            // this is used (or misused?) to obtain the deserialized multiview
            // topcomponent and set it to FormEditorSupport
            formEditorSupport.setTopComponent(callback.getTopComponent());
        }
    }

    public void requestVisible() {
        if (multiViewObserver != null)
            multiViewObserver.requestVisible();
        else
            super.requestVisible();
    }

    public void requestActive() {
        if (multiViewObserver != null)
            multiViewObserver.requestActive();
        else
            super.requestActive();
    }

    public void componentClosed() {
        super.componentClosed();
        if (formModel != null)
            setModel(null);
    }

    public void componentShowing() {
        super.componentShowing();
        if (!formEditorSupport.formLoaded) {
            formEditorSupport.loadFormDesigner();
        }
        if (!initialized) {
            initialize();
        }
        FormEditorSupport.checkFormGroupVisibility();
    }

    public void componentHidden() {
        super.componentHidden();
        FormEditorSupport.checkFormGroupVisibility();
    }

    public void componentOpened() {
        super.componentOpened();
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned designer, closing is OK
        if (!FormEditorSupport.isLastView(multiViewObserver.getTopComponent()))
            return CloseOperationState.STATE_OK;

        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
            "ID_FORM_CLOSING", // dummy ID // NOI18N
            MultiViewFactory.NOOP_CLOSE_ACTION,
            MultiViewFactory.NOOP_CLOSE_ACTION);
    }

    // -----------
    // innerclasses

    // Listener on FormModel - ensures updating of designer view.
    private class FormListener implements FormModelListener, Runnable {

        private FormModelEvent[] events;

        public void formChanged(final FormModelEvent[] events) {
            if (!EventQueue.isDispatchThread())
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        processEvents(events);
                    }
                });
            else
                processEvents(events);
        }

        private void processEvents(FormModelEvent[] events) {
            boolean lafBlock;
            if (events == null) {
                lafBlock = true;
            }
            else {
                lafBlock = false;
                boolean modifying = false;
                for (int i=0; i < events.length; i++) {
                    FormModelEvent ev = events[i];
                    if (ev.isModifying())
                        modifying = true;
                    if (ev.getChangeType() == FormModelEvent.COMPONENT_ADDED) {
                        lafBlock = true;
                        break;
                    }
                }
                if (!modifying)
                    return;
            }

            this.events = events;
            if (lafBlock) // Look&Feel UI defaults remapping needed
                FormLAF.executeWithLookAndFeel(this);
            else
                performUpdates();
        }

        public void run() {
            performUpdates();
        }

        private void performUpdates() {
            if (events == null) {
                replicator.setTopMetaComponent(topDesignComponent);
                Component formClone = (Component) replicator.createClone();
                if (formClone != null) {
                    formClone.setVisible(true);
                    componentLayer.setTopDesignComponent(formClone);
                    componentLayer.revalidate();
                    componentLayer.repaint();
                }
                return;
            }

            FormModelEvent[] events = this.events;
            this.events = null;

            int prevType = 0;
            ComponentContainer prevContainer = null;
            boolean updateDone = false;

            for (int i=0; i < events.length; i++) {
                FormModelEvent ev = events[i];
                int type = ev.getChangeType();
                ComponentContainer metacont = ev.getContainer();

                if (type == FormModelEvent.CONTAINER_LAYOUT_EXCHANGED
                    || type == FormModelEvent.CONTAINER_LAYOUT_CHANGED
                    || type == FormModelEvent.COMPONENT_LAYOUT_CHANGED)
                {
                    if ((prevType != FormModelEvent.CONTAINER_LAYOUT_EXCHANGED
                         && prevType != FormModelEvent.CONTAINER_LAYOUT_CHANGED
                         && prevType != FormModelEvent.COMPONENT_LAYOUT_CHANGED)
                        || prevContainer != metacont)
                    {
                        replicator.updateContainerLayout((RADVisualContainer)
                                                         metacont);
                        updateDone = true;
                    }
                }
                else if (type == FormModelEvent.COMPONENT_ADDED) {
                    if (prevType != FormModelEvent.COMPONENT_ADDED
                        || prevContainer != metacont)
                    {
                        replicator.updateAddedComponents(metacont);
                        updateDone = true;
                    }
                }
                else if (type == FormModelEvent.COMPONENT_REMOVED) {
                    RADComponent removed = ev.getComponent();

                    // if the top designed component (or some of its parents)
                    // was removed then whole designer must be recreated
                    if (removed instanceof RADVisualComponent
                        && (removed == topDesignComponent
                            || removed.isParentComponent(topDesignComponent)))
                    {
                        resetTopDesignComponent(false);
                        updateWholeDesigner();
                        return;
                    }
                    else {
                        replicator.removeComponent(ev.getComponent(),
                                                   ev.getContainer());
                        updateDone = true;
                    }
                }
                else if (type == FormModelEvent.COMPONENTS_REORDERED) {
                    if (prevType != FormModelEvent.COMPONENTS_REORDERED
                        || prevContainer != metacont)
                    {
                        replicator.reorderComponents(metacont);
                        updateDone = true;
                    }
                }
                else if (type == FormModelEvent.COMPONENT_PROPERTY_CHANGED) {
                    replicator.updateComponentProperty(
                                 ev.getComponentProperty());
                    updateDone = true;
                }
                else if (type == FormModelEvent.SYNTHETIC_PROPERTY_CHANGED
                         && PROP_DESIGNER_SIZE.equals(ev.getPropertyName()))
                    componentLayer.updateDesignerSize(getStoredDesignerSize());

                prevType = type;
                prevContainer = metacont;
            }

            if (updateDone) {
                componentLayer.revalidate();
                repaintSelection();
            }
        }
    }
    
    /** Lookup that excludes nodes. */
    private static class NoNodeLookup extends Lookup {
        private final Lookup delegate;
        
        public NoNodeLookup(Lookup delegate) {
            this.delegate = delegate;
        }
        
        public Object lookup(Class clazz) {
            return (clazz == Node.class) ? null : delegate.lookup(clazz);
        }
        
        public Lookup.Result lookup(Lookup.Template template) {
            if (template.getType() == Node.class) {
                return Lookup.EMPTY.lookup(new Lookup.Template(Node.class));
            } else {
                return delegate.lookup(template);
            }
        }
    }
    
}
