/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;

import org.openide.*;
import org.openide.windows.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerPanel;

import org.netbeans.modules.form.palette.*;
import org.netbeans.modules.form.fakepeer.FakePeerContainer;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;

import org.netbeans.lib.awtextra.*;

/**
 *
 * @author Tran Duc Trung, Tomas Pavek, Josef Kozak
 */

public class FormDesigner extends TopComponent
{
    public static final String PROP_DESIGNER_SIZE = "designerSize"; // NOI18N
    private JLayeredPane layeredPane;

    private ComponentLayer componentLayer;
    private HandleLayer handleLayer;
    private FormDesignerPanel fdPanel;

    private InPlaceEditLayer textEditLayer;
    private FormProperty editedProperty;

    private RADVisualComponent topDesignComponent;

    private JMenuBar formJMenuBar;
    private MenuBar formMenuBar;

    private FormModel formModel;
    private FormModelListener formModelListener;

    private FormEditorSupport formEditorSupport;

    private VisualReplicator replicator = new VisualReplicator(
        null,
        new Class[] { Window.class, Applet.class,
//                      RootPaneContainer.class,
                      MenuComponent.class },
        VisualReplicator.ATTACH_FAKE_PEERS | VisualReplicator.DISABLE_FOCUSING);

    private boolean updateTaskPlaced;

    private final ArrayList selectedComponents = new ArrayList();

    private RADComponent connectionSource;
    private RADComponent connectionTarget;
    
    /** The icons for FormDesigner */
    private static String iconURL = "org/netbeans/modules/form/resources/formDesigner.gif"; // NOI18N
    
    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = FormEditor.getFormSettings();
    

    public FormDesigner() {
        this(null);
    }

    void initialize() {
        updateWholeDesigner();
    }
    
    //////
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.formeditor"); // NOI18N
    }

    public Dimension getPreferredSize() {
        int padding = 2 * fdPanel.getBorderThickness();
        return new Dimension(400 + padding + 15, 300 + padding + 50);
    }

    //
    //
    //
    
    public void writeExternal(ObjectOutput out) throws IOException {
        if (formEditorSupport == null)
            return;

        super.writeExternal(out);
        out.writeObject(formEditorSupport.getFormDataObject());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object o = in.readObject();
        if (o instanceof FormDataObject) {
            formEditorSupport = ((FormDataObject)o).getFormEditor();
            formEditorSupport.setFormDesigner(this);

            // invoke loading in AWT event queue, but don't block it
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (formEditorSupport.loadForm()) {
                        setModel(formEditorSupport.getFormModel());
                        initialize();
                        ComponentInspector.getInstance().focusForm(formEditorSupport);
                    }
                }
            });
        }
    }

    public void open(Workspace workspace) {
        if (workspace == null)
            workspace = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
        
        if (isOpened(workspace))
            return;
        
        Mode mode = workspace.findMode("Form"); // NOI18N
        
        if (mode != null) {
            mode.dockInto(this);
        }

        super.open(workspace);
    }

    protected void componentActivated() {
        super.componentActivated();
        if (formModel == null)
            return;

        ComponentInspector ci = ComponentInspector.getInstance();
        if (ci.getFocusedForm() != formEditorSupport) {
            ComponentInspector.getInstance().focusForm(formEditorSupport);
            if (CPManager.getDefault().getMode() == PaletteAction.MODE_CONNECTION)
                clearSelection();
            else
                updateActivatedNodes();
        }

        ci.attachActions();
        if (textEditLayer == null || !textEditLayer.isVisible())
            handleLayer.requestFocus();
        else
            textEditLayer.requestFocus();
    }

    protected void componentDeactivated() {
        if (formModel == null)
            return;

        if (textEditLayer != null && textEditLayer.isVisible())
            textEditLayer.finishEditing(false);

        ComponentInspector.getInstance().detachActions();
        resetConnection();
        super.componentDeactivated();
    }

    public UndoRedo getUndoRedo() {
        UndoRedo ur = formModel != null ? formModel.getUndoRedoManager() : null;
        return ur != null ? ur : super.getUndoRedo();
    }

    void updateActivatedNodes() {
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
        }
        catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
            
        setActivatedNodes(selectedNodes);
    }

    void updateName(String name) {
        if (topDesignComponent != null
                && topDesignComponent != formModel.getTopRADComponent())
            name += " / " + topDesignComponent.getName(); // NOI18N
        if (formModel.isReadOnly())
            name += " " + FormEditor.getFormBundle().getString("CTL_FormTitle_RO"); // NOI18N
        setName(name);
        setToolTipText(name);
    }
    
    ////////////////
    
    FormDesigner(FormModel formModel) {
        // instruct winsys to save state of this top component only if opened
        putClientProperty("PersistenceType", "OnlyOpened"); // NOI18N
        
        setIcon(Utilities.loadImage(iconURL));
        
        formModelListener = new FormListener();
        
        componentLayer = new ComponentLayer();
        fdPanel = new FormDesignerPanel(formModel, componentLayer);
        
        FakePeerContainer fakeContainer = new FakePeerContainer();
        fakeContainer.setLayout(new BorderLayout());
        fakeContainer.add(fdPanel, BorderLayout.CENTER);
        
        handleLayer = new HandleLayer(this);
        
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.add(fakeContainer, new Integer(1000));
        layeredPane.add(handleLayer, new Integer(1001));

        setLayout(new BorderLayout());
        
        JScrollPane sp = new JScrollPane(layeredPane);
        add(sp);

        setModel(formModel);
    }

    void setModel(FormModel m) {
        if (formModel != null) {
            formModel.removeFormModelListener(formModelListener);
            topDesignComponent = null;
        }

        formModel = m;
        
        if (formModel != null) {
            formModel.addFormModelListener(formModelListener);
            formEditorSupport = FormEditorSupport.getSupport(formModel);
            resetTopDesignComponent(false);
            updateName(formModel.getName());
            handleLayer.setViewOnly(formModel.isReadOnly());
            fdPanel.updatePanel(getDesignerSize());
        }
        else formEditorSupport = null;
    }

    FormModel getModel() {
        return formModel;
    }

    FormEditorSupport getFormEditorSupport() {
        return formEditorSupport;
    }

    public Object getComponent(RADComponent metacomp) {
        return replicator.getClonedComponent(metacomp);
    }

    public RADComponent getMetaComponent(Object comp) {
        return replicator.getMetaComponent(comp);
    }

    java.util.List getSelectedComponents() {
        return selectedComponents;
    }

    void setSelectedComponent(RADComponent metacomp) {
        selectedComponents.clear();
        addComponentToSelectionImpl(metacomp);
        updateActivatedNodes();
    }

    void setSelectedComponents(RADComponent[] metacomps) {
        selectedComponents.clear();
        for (int i=0; i < metacomps.length; i++) {
            selectedComponents.add(metacomps[i]);
            if (metacomps[i] instanceof RADVisualComponent)
                ensureComponentIsShown((RADVisualComponent)metacomps[i]);
        }
        handleLayer.repaint();
        updateActivatedNodes();
    }

    void setSelectedNode(FormNode node) {
        if (node instanceof RADComponentNode)
            setSelectedComponent(((RADComponentNode)node).getRADComponent());
        else {
            clearSelectionImpl();

            ComponentInspector ci = ComponentInspector.getInstance();
            if (ci.getFocusedForm() != formEditorSupport)
                return;

            Node[] selectedNodes = new Node[] { node };
            try {
                ci.setSelectedNodes(selectedNodes, formEditorSupport);
            }
            catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }

            setActivatedNodes(selectedNodes);
        }
    }

    void addComponentToSelectionImpl(RADComponent metacomp) {
        if (metacomp == null)
            return;

        selectedComponents.add(metacomp);
        if (metacomp instanceof RADVisualComponent)
            ensureComponentIsShown((RADVisualComponent)metacomp);
        handleLayer.repaint();
    }

    void addComponentToSelection(RADComponent metacomp) {
        addComponentToSelectionImpl(metacomp);
        updateActivatedNodes();
    }

    void removeComponentFromSelectionImpl(RADComponent metacomp) {
        selectedComponents.remove(metacomp);
        handleLayer.repaint();
    }

    void removeComponentFromSelection(RADComponent metacomp) {
        removeComponentFromSelectionImpl(metacomp);
        updateActivatedNodes();
    }
    
    void clearSelectionImpl() {
        selectedComponents.clear();
        handleLayer.repaint();
    }

    public void clearSelection() {
        clearSelectionImpl();
        updateActivatedNodes();
    }

    boolean isComponentSelected(RADComponent metacomp) {
        return selectedComponents.contains(metacomp);
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
            LayoutSupportManager laysup = metacont.getLayoutSupport();
            Container contDelegate = metacont.getContainerDelegate(cont);

            laysup.selectComponent(child.getComponentIndex());
            laysup.arrangeContainer(cont, contDelegate);

            if (metacont == topDesignComponent || cont.isShowing())
                break;

            child = metacont;
            metacont = metacont.getParentContainer();
        }
    }

    /** Finds out what component follows after currently selected component
     * when TAB (forward true) or Shift+TAB (forward false) is pressed. 
     * @returns the next or previous component for selection
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

    /** @returns the next or prevoius component to component comp
     */
    RADVisualComponent getNextVisualComponent(RADVisualComponent comp,
                                              boolean forward) {
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

    private Dimension getDesignerSize() {
        RADComponent metacomp = formModel.getTopRADComponent();
        if (metacomp == null)
            return null;

        Dimension size = (Dimension) metacomp.getAuxValue(PROP_DESIGNER_SIZE);
        if (size == null)
            size = new Dimension(400, 300);
        return size;
    }

    private void setDesignerSize(Dimension size) {
        RADComponent metacomp = formModel.getTopRADComponent();
        if (metacomp instanceof RADVisualFormContainer)
            ((RADVisualFormContainer)metacomp).setDesignerSize(size);
        else if (metacomp != null)
            metacomp.setAuxValue(PROP_DESIGNER_SIZE, size);
    }

    // ------------------
    // designer content

    ComponentLayer getComponentLayer() {
        return componentLayer;
    }
    
    FormDesignerPanel getFormDesignerPanel() {
        return fdPanel;
    }    

    public void setTopDesignComponent(RADVisualComponent component,
                                      boolean update) {
        topDesignComponent = component;
        if (update) {
            setSelectedComponent(topDesignComponent);
            updateWholeDesigner();
        }
    }

    public RADVisualComponent getTopDesignComponent() {
        return topDesignComponent;
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
        updateName(formModel.getName());
    }

    public static Container createFormView(final RADVisualComponent metacomp,
                                           final Class contClass)
        throws Exception
    {
        return (Container) FormLAF.executeWithLookAndFeel(
            UIManager.getLookAndFeel().getClass().getName(),
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

    // ------------------
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
                    CPManager.getDefault().setMode(PaletteAction.MODE_SELECTION);                    
                }
                return;
            }
            connectionTarget = metacomp;
            handleLayer.repaint();
            if (showDialog) {
                if (connectionTarget != null) 
                    createConnection(connectionSource, connectionTarget);
//                resetConnection();
                CPManager.getDefault().setMode(PaletteAction.MODE_SELECTION);
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
            String bodyText = cw.getGeneratedCode();
            Event event = cw.getSelectedEvent();
            String eventName = cw.getEventName();
            EventHandler handler = null;

            for (Iterator iter = event.getHandlers().iterator(); iter.hasNext(); ) {
                EventHandler eh = (EventHandler) iter.next();
                if (eh.getName().equals(eventName)) {
                    handler = eh;
                    break;
                }
            }
            if (handler == null) { // new handler
                formModel.getFormEventHandlers().addEventHandler(event,
                                                                 eventName,
                                                                 bodyText);
            } else {
                handler.setHandlerText(bodyText);
            }
            event.gotoEventHandler(eventName);
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

        FormProperty property = metacomp.getPropertyByName("text"); // NOI18N
        if (property == null) return; // shoul not happen

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

//        layeredPane.remove(handleLayer);
//        layeredPane.add(textEditLayer, new Integer(2001));
        handleLayer.setVisible(false);
        textEditLayer.setVisible(true);
        layeredPane.revalidate();
        layeredPane.repaint();

        requestFocus();
        componentActivated();
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
//        layeredPane.remove(textEditLayer);
//        layeredPane.add(handleLayer, new Integer(1001));
        layeredPane.revalidate();
        layeredPane.repaint();
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
        TopManager.getDefault().notify(
            new NotifyDescriptor.Message(
                FormEditor.getFormBundle().getString("MSG_ComponentNotShown"), // NOI18N
                NotifyDescriptor.WARNING_MESSAGE));
    }

    // -----------
    // innerclasses

    // Listener on FormModel - ensures updating of designer view.
    private class FormListener implements FormModelListener, Runnable {
        private FormModelEvent[] events;

        public void formChanged(FormModelEvent[] events) {
            this.events = events; // we expect invoking in one thread...

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
                    if (ev.getChangeType() == FormModelEvent.COMPONENT_ADDED
                        && ev.getCreatedDeleted())
                    {
                        lafBlock = true;
                        break;
                    }
                }
                if (!modifying)
                    return;
            }

            if (lafBlock) { // Look&Feel UI defaults remapping needed
                try {
                    FormLAF.executeWithLookAndFeel(
                        UIManager.getLookAndFeel().getClass().getName(),
                        new Mutex.ExceptionAction () {
                            public Object run() throws Exception {
                                performUpdates();
                                return null;
                            }
                        }
                    );
                }
                catch (Exception ex) { // no exceptions expected
                    ex.printStackTrace();
                }
            }
            else if (!EventQueue.isDispatchThread()) {
                try {
                    EventQueue.invokeAndWait(this);
                }
                catch (Exception ex) { // ignore
                    ex.printStackTrace();
                }
            }
            else performUpdates();
        }

        public void run() {
            performUpdates();
        }

        private void performUpdates() {
            if (events == null) {
                componentLayer.removeAll();
                replicator.setTopMetaComponent(topDesignComponent);
                Component formClone = (Component) replicator.createClone();
                if (formClone != null) {
                    formClone.setVisible(true);
                    componentLayer.add(formClone, BorderLayout.CENTER);
                }
                updateName(formModel.getName());
                return;
            }

            FormModelEvent[] events = this.events;
            this.events = null;

            boolean updateDone = false;

            for (int i=0; i < events.length; i++) {
                FormModelEvent ev = events[i];
                int type = ev.getChangeType();

                if (type == FormModelEvent.CONTAINER_LAYOUT_EXCHANGED
                    || type == FormModelEvent.CONTAINER_LAYOUT_CHANGED
                    || type == FormModelEvent.COMPONENT_LAYOUT_CHANGED)
                {
                    replicator.updateContainerLayout((RADVisualContainer)
                                                     ev.getContainer());
                    updateDone = true;
                }
                else if (type == FormModelEvent.COMPONENT_ADDED) {
                    replicator.updateAddedComponents(ev.getContainer());
                    updateDone = true;
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
                    replicator.reorderComponents(ev.getContainer());
                    updateDone = true;
                }
                else if (type == FormModelEvent.COMPONENT_PROPERTY_CHANGED) {
                    replicator.updateComponentProperty(
                                 ev.getComponentProperty());
                    updateDone = true;
                }
                else if (type == FormModelEvent.SYNTHETIC_PROPERTY_CHANGED
                         && PROP_DESIGNER_SIZE.equals(ev.getPropertyName()))
                    fdPanel.updatePanel(getDesignerSize());
            }

            if (updateDone) {
                revalidate();
                repaint();
            }
        }
    }

    // -----------

    public static class FormDesignerPanel extends JPanel {
        
        private JComponent formDesignerLayer;
        private int borderThickness;
        private int lineThickness;
        private int paddingThickness;
        private FormModel formModel;
        private AbsoluteConstraints ac;
        
        
        public FormDesignerPanel(FormModel formModel, JComponent container) {
            this.formModel = formModel;
            
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(container, BorderLayout.CENTER);
            
            this.formDesignerLayer = panel;
            
            borderThickness = 5;
            lineThickness = 1;
            paddingThickness = 30;
            
            setLayout(new AbsoluteLayout());
            
            updateBackgroundColor();
            
            int borderSize = 2 * getBorderThickness();
            
            ac = new AbsoluteConstraints(0, 0, 400 + borderSize, 300 + borderSize);
            add(formDesignerLayer, ac);
        }
        
        
        public int getBorderThickness() {
            return borderThickness + lineThickness + paddingThickness;
        }
        
        
        public void updatePanel(Dimension d) {
            if (d == null) {
                return;
            }
            
            int padding = 2 * getBorderThickness();
            ac.width = d.width + padding;
            ac.height = d.height + padding;

            formDesignerLayer.revalidate();
            formDesignerLayer.repaint();
        }
        
        
        public void updateBackgroundColor() {
            setBackground(formSettings.getFormDesignerBackgroundColor());
            updateBorderColor();
        }
    
        
        public void updateBorderColor() {
            CompoundBorder border = new CompoundBorder(
                new CompoundBorder(
                    new LineBorder(formSettings.getFormDesignerBackgroundColor(), 30), 
                    new LineBorder(formSettings.getFormDesignerBackgroundColor(), 1)), //java.awt.Color.black ??
                new LineBorder(formSettings.getFormDesignerBorderColor(), 5));
            
            formDesignerLayer.setBorder(border); 
        }
    }

    
    public static class Resizer  {

        private FormDesigner formDesigner;
        private FormDesignerPanel fdPanel;
        private int resize;
        
        
        public Resizer(FormDesigner formDesigner, int resize) {
            this.formDesigner = formDesigner;
            this.resize = resize;
            this.fdPanel = formDesigner.getFormDesignerPanel();
        }
        
        
        private void setStatusText(String formatId, Object[] args) {
            TopManager.getDefault().setStatusText(
                java.text.MessageFormat.format(
                    FormEditor.getFormBundle().getString(formatId), args));
        }
        
        
        public void showCurrentSizeInStatus() {
            Dimension size = fdPanel.getPreferredSize();
            int padding = 2 * fdPanel.getBorderThickness();
            setStatusText("FMT_MSG_RESIZING_FORMDESIGNER", // NOI18N
                            new Object[] { new Integer(size.width - padding).toString(), 
                                           new Integer(size.height - padding).toString() } );            
        }
        
        
        public void hideCurrentSizeInStatus() {
            TopManager.getDefault().setStatusText(""); // NOI18N
        }
        
        
        public void dropDesigner(Point p, boolean resizingFinished) {
            int w = fdPanel.getPreferredSize().width;
            int h = fdPanel.getPreferredSize().height;
            int border = fdPanel.getBorderThickness();
            
            if (resize == (LayoutSupportManager.RESIZE_DOWN
                           | LayoutSupportManager.RESIZE_RIGHT))
            {
                w = p.x - border;
                h = p.y - border;
            }
            else if (resize == LayoutSupportManager.RESIZE_DOWN) {
                w = w - 2 * border;
                h = p.y - border;
            }
            else if (resize == LayoutSupportManager.RESIZE_RIGHT) {
                w = p.x - border;
                h = h - 2 * border;
            }
            
            int minSize = 20;
            if (w < minSize) w = minSize;
            if (h < minSize) h = minSize;
            
            fdPanel.updatePanel(new Dimension(w, h));
            
            if (resizingFinished)
                formDesigner.setDesignerSize(new Dimension(w, h));
            
            setStatusText("FMT_MSG_RESIZING_FORMDESIGNER", // NOI18N
                            new Object[] { new Integer(w).toString(), new Integer(h).toString() } );
        }
    }
}
