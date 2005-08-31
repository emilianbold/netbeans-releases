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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import java.beans.*;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.layout.*;

import org.netbeans.core.spi.multiview.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FileSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.ExplorerManager;

import org.netbeans.modules.form.wizard.ConnectionWizard;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.netbeans.modules.form.layoutdesign.*;
import org.netbeans.modules.form.palette.PaletteUtils;

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

    // UI components composition
    private JLayeredPane layeredPane;
    private ComponentLayer componentLayer;
    private HandleLayer handleLayer;
    private NonVisualTray nonVisualTray;
    private FormToolBar formToolBar;
    
    // in-place editing
    private InPlaceEditLayer textEditLayer;
    private FormProperty editedProperty;

    // metadata
    private FormModel formModel;
    private FormModelListener formModelListener;
    private RADVisualComponent topDesignComponent;

    private FormEditor formEditor;

    // layout visualization and interaction
    private List selectedComponents = new ArrayList();
    private List selectedLayoutComponents = new ArrayList();
    private VisualReplicator replicator;
    private LayoutDesigner layoutDesigner;
    private List designerActions;
    private List resizabilityActions;
    
    private JToggleButton[] resizabilityButtons;
            
    private int designerMode;
    static final int MODE_SELECT = 0;
    static final int MODE_CONNECT = 1;
    static final int MODE_ADD = 2;
    
    private boolean initialized = false;

    private RADComponent connectionSource;
    private RADComponent connectionTarget;

    MultiViewElementCallback multiViewObserver;

    private ExplorerManager explorerManager;
    private FormProxyLookup lookup;

    /** The icons for FormDesigner */
    private static String iconURL =
        "org/netbeans/modules/form/resources/formDesigner.gif"; // NOI18N

    // ----------
    // constructors and setup
    
    FormDesigner(FormEditor formEditor) {
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

        this.formEditor = formEditor;
        explorerManager = new ExplorerManager();

        // add FormDataObject to lookup so it can be obtained from multiview TopComponent
        ActionMap map = ComponentInspector.getInstance().setupActionMap(getActionMap());
        FormDataObject formDataObject = formEditor.getFormDataObject();
        lookup = new FormProxyLookup(new Lookup[] {
            ExplorerUtils.createLookup(explorerManager, map),
            Lookups.fixed(new Object[] { formDataObject,  PaletteUtils.getPalette() }),
            formDataObject.getNodeDelegate().getLookup()
        });
        associateLookup(lookup);

        formToolBar = new FormToolBar(this);
    }
    
    private void initialize() {
        initialized = true;
        removeAll();

        componentLayer = new ComponentLayer();
        handleLayer = new HandleLayer(this);
        nonVisualTray = FormEditor.isNonVisualTrayEnabled() ?
                        new NonVisualTray(formEditor.getFormModel()) : null;

        JPanel designPanel = new JPanel(new BorderLayout());
        designPanel.add(componentLayer, BorderLayout.CENTER);
        if (nonVisualTray != null) {
            designPanel.add(nonVisualTray, BorderLayout.SOUTH);
        }
        
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.add(designPanel, new Integer(1000));
        layeredPane.add(handleLayer, new Integer(1001));

        JScrollPane scrollPane = new JScrollPane(layeredPane);
        scrollPane.setBorder(null); // disable border, winsys will handle borders itself
        scrollPane.getVerticalScrollBar().setUnitIncrement(5); // Issue 50054
        scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
        add(scrollPane, BorderLayout.CENTER);

        explorerManager.setRootContext(formEditor.getFormRootNode());
        addPropertyChangeListener("activatedNodes", new PropertyChangeListener() { // NOI18N
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    Lookup[] lookups = lookup.getSubLookups();
                    Node[] oldNodes = (Node[])evt.getOldValue();
                    Node[] nodes = (Node[])evt.getNewValue();
                    Lookup lastLookup = lookups[lookups.length-1];
                    Node delegate = formEditor.getFormDataObject().getNodeDelegate();
                    if (!(lastLookup instanceof NoNodeLookup)
                        && (oldNodes.length >= 1)
                        && (!oldNodes[0].equals(delegate))) {
                        switchLookup();
                    } else if ((lastLookup instanceof NoNodeLookup)
                        && (nodes.length == 0)) {
                        switchLookup();
                    }
                    List list = new ArrayList(nodes.length);
                    list.addAll(Arrays.asList(nodes));
                    list.remove(delegate);
                    explorerManager.setSelectedNodes((Node[])list.toArray(new Node[list.size()]));
                } catch (PropertyVetoException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        formModel = formEditor.getFormModel();
        if (formModelListener == null)
            formModelListener = new FormListener();
        formModel.addFormModelListener(formModelListener);

        replicator = new VisualReplicator(
            null,
            new Class[] { Window.class,
                          java.applet.Applet.class,
                          MenuComponent.class },
            VisualReplicator.ATTACH_FAKE_PEERS | VisualReplicator.DISABLE_FOCUSING);

        resetTopDesignComponent(false);
        handleLayer.setViewOnly(formModel.isReadOnly());

        // Beans without layout model doesn't require layout designer
        if (formModel.getLayoutModel() != null) {
            layoutDesigner = new LayoutDesigner(formModel.getLayoutModel(),
                                            new LayoutMapper());
        }
        
        updateWholeDesigner();
        
        // not very nice hack - it's better FormEditorSupport has its
        // listener registered after FormDesigner
        formEditor.reinstallListener();
    }
    
    private void switchLookup() {
        Lookup[] lookups = lookup.getSubLookups();
        Lookup nodeLookup = formEditor.getFormDataObject().getNodeDelegate().getLookup();
        int index = lookups.length - 1;
        if (lookups[index] instanceof NoNodeLookup) {
            lookups[index] = nodeLookup;
        } else {
            // should not affect selected nodes, but should provide cookies etc.
            lookups[index] = new NoNodeLookup(nodeLookup);
        }
        lookup.setSubLookups(lookups);
    }

    // ------
    // important getters

    FormModel getFormModel() {
        return formModel;
    }

    HandleLayer getHandleLayer() {
        return handleLayer;
    }

    ComponentLayer getComponentLayer() {
        return componentLayer;
    }
    
    NonVisualTray getNonVisualTray() {
        return nonVisualTray;
    }

    FormToolBar getFormToolBar() {
        return formToolBar;
    }

    public LayoutDesigner getLayoutDesigner() {
        return layoutDesigner;
    }
    
    FormEditor getFormEditor() {
        return formEditor;
    }
    
    public javax.swing.Action[] getActions() {
        Action[] actions = super.getActions();
        SystemAction fsAction = SystemAction.get(FileSystemAction.class);
        if (!Arrays.asList(actions).contains(fsAction)) {
            Action[] newActions = new Action[actions.length+1];
            System.arraycopy(actions, 0, newActions, 0, actions.length);
            newActions[actions.length] = fsAction;
            actions = newActions;
        }
        return actions;
    }

    // ------------
    // designer content

    public Object getComponent(RADComponent metacomp) {
        return replicator.getClonedComponent(metacomp.getId());
    }

    public Object getComponent(String componentId) {
        return replicator.getClonedComponent(componentId);
    }

    public RADComponent getMetaComponent(Object comp) {
        String id = replicator.getClonedComponentId(comp);
        return id != null ? formModel.getMetaComponent(id) : null;
    }

//    public RADComponent getMetaComponent(String componentId) {
//        return formModel.getMetaComponent(componentId);
//    }

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
        setTopDesignComponent(
            formModel.getTopRADComponent() instanceof RADVisualComponent ?
                    (RADVisualComponent) formModel.getTopRADComponent() : null,
            update);
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

    private void setupDesignerSize(Component comp) {
        Dimension size = null;
        RADVisualFormContainer formCont = null;
        if (topDesignComponent instanceof RADVisualFormContainer) {
            formCont = (RADVisualFormContainer) topDesignComponent;
            if (!formModel.isFreeDesignDefaultLayout()
                || formCont.getFormSizePolicy() == RADVisualFormContainer.GEN_BOUNDS)
            {   // use hardcoded number for designer size
                size = formCont.getDesignerSize();
            }
        }
        if (size == null && formModel.isFreeDesignDefaultLayout()) {
//            topDesignComponent instanceof RADVisualContainer && ((RADVisualContainer)topDesignComponent).getLayoutSupport() == null
            // compute designer size from preferred size of top component
            size = comp.getPreferredSize();
            Dimension storedSize = getStoredDesignerSize();
            if (!size.equals(storedSize)) {
                // remember the size in metadata
                if (formCont != null) {
                    formCont.setFormSize(null);
                    formCont.setDesignerSize(size);
                }
                else if (topDesignComponent == formModel.getTopRADComponent()) {
                    topDesignComponent.setAuxValue(PROP_DESIGNER_SIZE, size);
                }
            }
        }
        else size = (Dimension) topDesignComponent.getAuxValue(PROP_DESIGNER_SIZE);

        componentLayer.setDesignerSize(size);
    }

    public void updateDesignerSize() {
        if (!(topDesignComponent instanceof RADVisualContainer))
            return;

//        formModel.fireContainerLayoutChanged((RADVisualContainer)topDesignComponent, null, null, null);
        updateLayoutRecursively((RADVisualContainer)topDesignComponent);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setupDesignerSize(getTopVisualContainer());
            }
        });
//        updateComponentLayer();
    }

    private void updateLayoutRecursively(RADVisualContainer metacont) {
        RADVisualComponent[] subComps = metacont.getSubComponents();
        for (int i=0; i < subComps.length; i++) {
            if (subComps[i] instanceof RADVisualContainer)
                updateLayoutRecursively((RADVisualContainer)subComps[i]);
        }
        formModel.fireContainerLayoutChanged(metacont, null, null, null);
//        replicator.updateContainerLayout(metacont);
    }

    void updateComponentLayer() {
        if (getLayoutDesigner() == null) return;
        componentLayer.revalidate();

        // after the components are layed out, sync the layout designer
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (getLayoutDesigner().updateCurrentState()) {
                    formModel.fireFormChanged(); // hack: to regenerate code once again
                }
                updateResizabilityActions();
                componentLayer.repaint();
            }
        });
    }

    // updates layout of a container - used by HandleLayer when starting and
    // canceling component dragging - it changes the layout model temporarily
    // without changing the meta-components
    void updateContainerLayout(RADVisualContainer metacont, boolean revalidate) {
        replicator.updateContainerLayout(metacont);
        if (revalidate) {
            updateComponentLayer();
        }
        else {
            componentLayer.repaint();
        }
    }

    public static Container createFormView(final RADVisualComponent metacomp,
                                           final Class contClass)
        throws Exception
    {
        return (Container) FormLAF.executeWithLookAndFeel(
            new Mutex.ExceptionAction () {
                public Object run() throws Exception {
                    VisualReplicator r = new VisualReplicator(
                        contClass, null, 0);
                    r.setTopMetaComponent(metacomp);
                    return r.createClone();
                }
            }
        );
    }

    Container getTopVisualContainer() {
        RADVisualComponent topComp = replicator.getTopMetaComponent();
        if (!(topComp instanceof RADVisualContainer))
            return null;

        return ((RADVisualContainer)topComp).getContainerDelegate(
                             replicator.getClonedComponent(topComp));
    }

    // NOTE: does not create a new Point instance
    Point pointFromComponentToHandleLayer(Point p, Component sourceComp) {
        Component commonParent = layeredPane;
        Component comp = sourceComp;
        while (comp != commonParent) {
            p.x += comp.getX();
            p.y += comp.getY();
            comp = comp.getParent();
        }
        comp = handleLayer;
        while (comp != commonParent) {
            p.x -= comp.getX();
            p.y -= comp.getY();
            comp = comp.getParent();
        }
        return p;
    }

    // NOTE: does not create a new Point instance
    Point pointFromHandleToComponentLayer(Point p, Component targetComp) {
        Component commonParent = layeredPane;
        Component comp = handleLayer;
        while (comp != commonParent) {
            p.x += comp.getX();
            p.y += comp.getY();
            comp = comp.getParent();
        }
        comp = targetComp;
        while (comp != commonParent) {
            p.x -= comp.getX();
            p.y -= comp.getY();
            comp = comp.getParent();
        }
        return p;
    }

    private Rectangle componentBoundsToTop(Component component) {
        if (component == null)
            return null;

        Component top = getTopVisualContainer();

        int dx = 0;
        int dy = 0;

        if (component != top) {
            Component comp = component.getParent();
            while (comp != top) {
                if (comp == null) {
                    break;//return null;
                }
                dx += comp.getX();
                dy += comp.getY();
                comp = comp.getParent();
            }
        }
        else {
            dx = -top.getX();
            dy = -top.getY();
        }

        Rectangle bounds = component.getBounds();
        bounds.x += dx;
        bounds.y += dy;

        return bounds;
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

        handleLayer.endDragging(null);
        updateResizabilityActions();
    }

    int getDesignerMode() {
        return designerMode;
    }

    void toggleSelectionMode() {
        setDesignerMode(MODE_SELECT);
        PaletteUtils.clearPaletteSelection();
    }

    void toggleConnectionMode() {
        setDesignerMode(MODE_CONNECT);
        PaletteUtils.clearPaletteSelection();
    }

    void toggleAddMode() {
        setDesignerMode(MODE_ADD);
        PaletteUtils.clearPaletteSelection();
    }

    // -------
    // designer size

    Dimension getStoredDesignerSize() {
        RADComponent metacomp = formModel.getTopRADComponent();
        if (metacomp instanceof RADVisualFormContainer) {
            return ((RADVisualFormContainer)metacomp).getDesignerSize();
        }
        else if (metacomp != null) {
            return (Dimension) metacomp.getAuxValue(PROP_DESIGNER_SIZE);
        }
        else return null;
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

    java.util.List getSelectedLayoutComponents() {
        return selectedLayoutComponents;
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
            if (ci.getFocusedForm() != formEditor)
                return;

            Node[] selectedNodes = new Node[] { node };
            try {
                ci.setSelectedNodes(selectedNodes, formEditor);
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
            if (metacomp instanceof RADVisualComponent) {
                RADComponent metacont = metacomp.getParentComponent();
                if ((metacont != null) && JScrollPane.class.isAssignableFrom(metacont.getBeanInstance().getClass())) {
                    metacomp = metacont;
                }
                RADComponent root = metacomp;
                while (root.getParentComponent() != null) {
                    root = root.getParentComponent();
                }
                // Avoid visual components in others components
                if (root == formModel.getTopRADComponent()) {
                    selectedLayoutComponents.add(metacomp);
                }
                ensureComponentIsShown((RADVisualComponent)metacomp);
                selectionChanged();
            }
        }
    }

    void removeComponentFromSelectionImpl(RADComponent metacomp) {
        selectedComponents.remove(metacomp);
        selectedLayoutComponents.remove(metacomp);
        selectionChanged();
    }

    void clearSelectionImpl() {
        selectedComponents.clear();
        selectedLayoutComponents.clear();
        selectionChanged();
    }

    void selectionChanged() {
        updateDesignerActions();
        updateResizabilityActions();
    }

    void repaintSelection() {
        handleLayer.repaint();
    }

    private void updateDesignerActions() {
        Collection selectedIds = selectedLayoutComponentIds();
        boolean enabled = false;
        if (selectedIds.size() >= 2) {
            RADComponent parent = commonParent(selectedIds);
            if (parent != null) {
                LayoutModel layoutModel = formModel.getLayoutModel();
                LayoutComponent parentLC = layoutModel.getLayoutComponent(parent.getId());
                if ((parentLC != null) && (parentLC.isLayoutContainer())) {
                    enabled = true;
                }
            }
        }
        Iterator iter = getDesignerActions(true).iterator();
        while (iter.hasNext()) {
            Action action = (Action)iter.next();
            action.setEnabled(enabled);
        }
    }

    void setResizabilityButtons(JToggleButton[] buttons) {
        this.resizabilityButtons = buttons;
    }
    
    public JToggleButton[] getResizabilityButtons() {
        return resizabilityButtons;
    }

    public void updateResizabilityActions() {
        Collection componentIds = componentIds();
        LayoutModel layoutModel = getFormModel().getLayoutModel();
        LayoutDesigner layoutDesigner = getLayoutDesigner();
        Iterator iter = componentIds.iterator();
        boolean resizable[] = new boolean[2];
        boolean nonResizable[] = new boolean[2];
        while (iter.hasNext()) {
            String id = (String)iter.next();
            LayoutComponent comp = layoutModel.getLayoutComponent(id);
            for (int i=0; i<2; i++) {
                if (layoutDesigner.isComponentResizing(comp,
                        (i == 0) ? LayoutConstants.HORIZONTAL : LayoutConstants.VERTICAL)) {
                    resizable[i] = true;
                } else {
                    nonResizable[i] = true;
                }
            }
        }
        for (int i=0; i<2; i++) {
            boolean match;
            boolean miss;
            match = resizable[i];
            miss = nonResizable[i];
            getResizabilityButtons()[i].setSelected(!miss && match);
            ((ResizabilityAction)getResizabilityActions().toArray()[i]).setEnabled(match || miss);
//                getResizabilityButtons()[i].setPaintDisabledIcon(match && miss);
        }
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
    
    /**
     * Aligns selected components in the specified direction.
     *
     * @param closed determines if closed group should be created.
     * @param dimension dimension to align in.
     * @param alignment requested alignment.
     */
    void align(boolean closed, int dimension, int alignment) {
        // Check that the action is enabled
        Action action = null;
        Iterator iter = getDesignerActions(true).iterator();
        while (iter.hasNext()) {
            Action candidate = (Action)iter.next();
            if (candidate instanceof AlignAction) {
                AlignAction alignCandidate = (AlignAction)candidate;
                if ((alignCandidate.getAlignment() == alignment) && (alignCandidate.getDimension() == dimension)) {
                    action = alignCandidate;
                    break;
                }
            }
        }
        if ((action == null) || (!action.isEnabled())) {
            return;
        }
        Collection selectedIds = selectedLayoutComponentIds();
        RADComponent parent = commonParent(selectedIds);
        LayoutModel layoutModel = formModel.getLayoutModel();
        Object layoutUndoMark = layoutModel.getChangeMark();
        javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
        getLayoutDesigner().align(selectedIds, closed, dimension, alignment);
        formModel.fireContainerLayoutChanged((RADVisualContainer)parent, null, null, null);
        if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
            formModel.addUndoableEdit(ue);
        }
    }
    
    /**
     * Returns designer actions (they will be displayed in toolbar).
     *
     * @param forToolbar determines whether the method should return
     * all designer actions or just the subset for the form toolbar.
     * @return <code>Collection</code> of <code>Action</code> objects.
     */
    public Collection getDesignerActions(boolean forToolbar) {
        if (designerActions == null) {
            designerActions = new LinkedList();
            // Grouping actions
            designerActions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.LEADING, true));
            designerActions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.TRAILING, true));
            designerActions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.LEADING, true));
            designerActions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.TRAILING, true));
            // Align actions
            designerActions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.LEADING, false));
            designerActions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.TRAILING, false));
            designerActions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.LEADING, false));
            designerActions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.TRAILING, false));
        }
        return forToolbar ? designerActions.subList(0, 4) : designerActions;
    }
    
    public Collection getResizabilityActions() {
        if (resizabilityActions == null) {
            resizabilityActions = new LinkedList();
            resizabilityActions.add(new ResizabilityAction(LayoutConstants.HORIZONTAL));
            resizabilityActions.add(new ResizabilityAction(LayoutConstants.VERTICAL));
        }
        return resizabilityActions;
    }
    
    /**
     * Returns collection of ids of the selected layout components.
     *
     * @return <code>Collection</code> of <code>String</code> objects.
     */
    Collection selectedLayoutComponentIds() {
        Iterator metacomps = getSelectedLayoutComponents().iterator();
        Collection selectedIds = new LinkedList();
        while (metacomps.hasNext()) {
            RADComponent metacomp = (RADComponent)metacomps.next();
            selectedIds.add(metacomp.getId());
        }
        return selectedIds;
    }
    
    /**
     * Checks whether the given components are in the same containter.
     *
     * @param compIds <code>Collection</code> of component IDs.
     * @return common container parent or <code>null</code>
     * if the components are not from the same container.
     */
    private RADComponent commonParent(Collection compIds) {
        RADComponent parent = null;
        Iterator iter = compIds.iterator();
        FormModel formModel = getFormModel();
        while (iter.hasNext()) {
            String compId = (String)iter.next();
            RADComponent metacomp = formModel.getMetaComponent(compId);
            RADComponent metacont = metacomp.getParentComponent();
            if (parent == null) {
                parent = metacont;
            }
            if ((metacont == null) || (parent != metacont)) {
                return null;
            }
        }
        return parent;
    }

    // ---------
    // visibility update

    // synchronizes ComponentInspector with selection in FormDesigner
    // [there is a hardcoded relationship between these two views]
    void updateComponentInspector() {
        ComponentInspector ci = ComponentInspector.getInstance();
        if (ci.getFocusedForm() != formEditor)
            return;

        Node[] selectedNodes = new Node[selectedComponents.size()];
        Iterator iter = selectedComponents.iterator();
        int i = 0;
        while (iter.hasNext()) {
            RADComponent metacomp = (RADComponent) iter.next();
            selectedNodes[i++] = metacomp.getNodeReference();
        }
        try {
            ci.setSelectedNodes(selectedNodes, formEditor);
            // sets also the activated nodes (both for ComponentInspector
            // and FormDesigner)
        }
        catch (java.beans.PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }

    void updateVisualSettings() {
        componentLayer.updateVisualSettings();
        if (nonVisualTray != null) {
            nonVisualTray.updateVisualSettings();
        }
        layeredPane.revalidate();
        layeredPane.repaint(); // repaints both HanleLayer and ComponentLayer
    }

    private void ensureComponentIsShown(RADVisualComponent metacomp) {
        Component comp = (Component) getComponent(metacomp);
        if (comp == null)
            return; // component is not in the visualized tree

//        if (comp == null) { // visual component doesn't exist yet
//            if (metacont != null)
//                metacont.getLayoutSupport().selectComponent(
//                               metacont.getIndexOf(metacomp));
//            return;
//        }

        if (comp.isShowing())
            return; // component is showing
        if (!isInDesignedTree(metacomp))
            return; // component is not in designer

        Component topComp = (Component) getComponent(topDesignComponent);
        if (topComp == null || !topComp.isShowing())
            return; // designer is not showing

        RADVisualContainer metacont = metacomp.getParentContainer();
        RADVisualComponent child = metacomp;

        while (metacont != null) {
            Container cont = (Container) getComponent(metacont);

            LayoutSupportManager laysup = metacont.getLayoutSupport();
            if (laysup != null) {
                Container contDelegate = metacont.getContainerDelegate(cont);
                laysup.selectComponent(child.getComponentIndex());
                laysup.arrangeContainer(cont, contDelegate);
            }

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

    // only MultiViewDescriptor is stored, not MultiViewElement
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.formeditor"); // NOI18N
    }

    public void componentActivated() {
        if (formModel == null)
            return;

        formEditor.setFormDesigner(this);
        ComponentInspector ci = ComponentInspector.getInstance();
        if (ci.getFocusedForm() != formEditor) {
            ci.focusForm(formEditor);
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
        return formEditor.getFormDataObject().getName();
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
        if (formEditor != null) {
            // this is used (or misused?) to obtain the deserialized multiview
            // topcomponent and set it to FormEditorSupport
            FormDataObject formDO = formEditor.getFormDataObject();
            formDO.getFormEditorSupport().setTopComponent(callback.getTopComponent());
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
        if (formModel != null) {
            if (formModelListener != null)
                formModel.removeFormModelListener(formModelListener);
            topDesignComponent = null;
            formModel = null;
        }
    }

    public void componentShowing() {
        super.componentShowing();
        if (!formEditor.isFormLoaded()) {
            formEditor.loadFormDesigner();
            if (!formEditor.isFormLoaded()) { // there was a loading error
                removeAll();
                return;
            }
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

    private class LayoutMapper implements VisualMapper, LayoutConstants {

        private LayoutStyle layoutStyle;

        // -------

//        public String getTopComponentId() {
//            return getTopDesignComponent().getId();
//        }

        public Rectangle getComponentBounds(String componentId) {
            Component visual = getVisualComponent(componentId, true, false);
            return visual != null ? componentBoundsToTop(visual) : null;
        }

        public Rectangle getContainerInterior(String componentId) {
            Component visual = getVisualComponent(componentId, true, false);
            if (visual == null)
                return null;

            RADVisualContainer metacont = (RADVisualContainer)
                                          getMetaComponent(componentId);
            Container cont = metacont.getContainerDelegate(visual);

            Rectangle rect = componentBoundsToTop(cont);
            Insets insets = cont.getInsets();
            rect.x += insets.left;
            rect.y += insets.top;
            rect.width -= insets.left + insets.right;
            rect.height -= insets.top + insets.bottom;

            return rect;
        }

        public Dimension getComponentMinimumSize(String componentId) {
            Component visual = getVisualComponent(componentId, false, false);
            return visual != null ? visual.getMinimumSize() : null;
        }

        public Dimension getComponentPreferredSize(String componentId) {
            Component visual = getVisualComponent(componentId, false, false);
            return visual != null ? visual.getPreferredSize() : null;
        }

        public boolean hasExplicitPreferredSize(String componentId) {
            JComponent visual = (JComponent) getVisualComponent(componentId, false, true);
            return visual != null ? visual.isPreferredSizeSet() : false;
        }

        public int getBaselinePosition(String componentId) {
            JComponent comp = (JComponent) getVisualComponent(componentId, true, true);
            // [hack - vertically resizable components cannot be baseline aligned]
            // [this should be either solved or filtered in LayoutDragger according to vertical resizability of the component]
            if (comp instanceof JScrollPane || comp instanceof JPanel || comp instanceof JTabbedPane) {
//                    || comp instanceof JTextArea
//                    || comp instanceof JTree || comp instanceof JTable || comp instanceof JList
                return 0;
            }
                
            return comp != null ? Baseline.getBaseline(comp) : 0;
        }

        public int getPreferredPadding(String comp1Id,
                                       String comp2Id,
                                       int dimension,
                                       int comp2Alignment,
                                       int paddingType)
        {
            JComponent comp1 = (JComponent) getVisualComponent(comp1Id, true, true);
            JComponent comp2 = (JComponent) getVisualComponent(comp2Id, true, true);
            if (comp1 == null || comp2 == null) // not JComponents...
                return 10; // default distance between components (for non-JComponents)

            assert dimension == HORIZONTAL || dimension == VERTICAL;
            assert comp2Alignment == LEADING || comp2Alignment == TRAILING;
            assert paddingType == PADDING_RELATED || paddingType == PADDING_UNRELATED || paddingType == INDENT;

            int type = paddingType == INDENT ? LayoutStyle.INDENT :
                (paddingType == PADDING_RELATED ? LayoutStyle.RELATED : LayoutStyle.UNRELATED);
            int position = 0;
            if (dimension == HORIZONTAL) {
                if (paddingType == INDENT) {
                    position = comp2Alignment == LEADING ?
                               SwingConstants.WEST : SwingConstants.EAST;
                } else {
                    position = comp2Alignment == LEADING ?
                               SwingConstants.EAST : SwingConstants.WEST;
                }
            }
            else {
                position = comp2Alignment == LEADING ?
                           SwingConstants.SOUTH : SwingConstants.NORTH;
            }

            return getLayoutStyle().getPreferredGap(comp1, comp2, type, position, null);
        }

        public int getPreferredPaddingInParent(String parentId,
                                               String compId,
                                               int dimension,
                                               int compAlignment)
        {
            JComponent comp = null;
            Container parent = (Container)getVisualComponent(parentId, true, false);
            if (parent != null) {
                RADVisualContainer metacont = (RADVisualContainer)
                                              getMetaComponent(parentId);
                parent = metacont.getContainerDelegate(parent);
                comp = (JComponent) getVisualComponent(compId, true, true);
            }
            if (comp == null)
                return 10; // default distance from parent border (for non-JComponents)

            assert dimension == HORIZONTAL || dimension == VERTICAL;
            assert compAlignment == LEADING || compAlignment == TRAILING;

            int alignment;

            if (dimension == HORIZONTAL) {
                if (compAlignment == LEADING) {
                    alignment = SwingConstants.WEST;
                }
                else {
                    alignment = SwingConstants.EAST;
                }
            }
            else {
                if (compAlignment == LEADING) {
                    alignment = SwingConstants.NORTH;
                }
                else {
                    alignment = SwingConstants.SOUTH;
                }
            }
            return getLayoutStyle().getContainerGap(comp, alignment, parent);
        }

        public boolean[] getComponentResizability(String compId, boolean[] resizability) {
            resizability[0] = resizability[1] = true;
            // [real resizability spec TBD]
            return resizability;
        }

        public void rebuildLayout(String contId) {
            replicator.updateContainerLayout((RADVisualContainer)getMetaComponent(contId));
            replicator.getLayoutBuilder(contId).doLayout();
        }

        // -------

        private RADComponent getMetaComponent(String compId) {
            RADComponent metacomp = formModel.getMetaComponent(compId);
            if (metacomp == null) {
                RADComponent precreated =
                    formModel.getComponentCreator().getPrecreatedMetaComponent();
                if (precreated != null && precreated.getId().equals(compId)) {
                    metacomp = precreated;
                }
            }
            return metacomp;
        }

        private Component getVisualComponent(String compId, boolean needVisible, boolean needJComponent) {
            Object comp = getComponent(compId);
            if (comp == null) {
                RADVisualComponent precreated =
                    formModel.getComponentCreator().getPrecreatedMetaComponent();
                if (precreated != null && precreated.getId().equals(compId)) {
                    comp = precreated.getBeanInstance();
                }
                if (comp == null && !needVisible) {
                    RADComponent metacomp = getMetaComponent(compId);
                    if (metacomp != null) {
                        comp = metacomp.getBeanInstance();
                    }
                }
            }
            Class type = needJComponent ? JComponent.class : Component.class;
            return comp != null && type.isAssignableFrom(comp.getClass()) ?
                   (Component) comp : null;
        }

        private LayoutStyle getLayoutStyle() {
            if (layoutStyle == null)
                layoutStyle = LayoutStyle.getSharedInstance();
            return layoutStyle;
        }
    }

    // --------

    private Collection componentIds() {
        List componentIds = new LinkedList();
        List selectedComps = getSelectedLayoutComponents();
        Iterator iter = selectedComps.iterator();
        while (iter.hasNext()) {
            RADVisualComponent visualComp = (RADVisualComponent)iter.next();
            if ((visualComp.getParentContainer() != null)
            && (visualComp.getParentLayoutSupport() == null))
                componentIds.add(visualComp.getId());
        }
        return componentIds;
    }
    
    // Listener on FormModel - ensures updating of designer view.
    private class FormListener implements FormModelListener, Runnable {

        private FormModelEvent[] events;

        public void formChanged(final FormModelEvent[] events) {
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

                assert EventQueue.isDispatchThread();
            }

            this.events = events;

            if (lafBlock) // Look&Feel UI defaults remapping needed
                FormLAF.executeWithLookAndFeel(this);
            else
                run();
        }

        public void run() {
            if (events == null) {
                replicator.setTopMetaComponent(topDesignComponent);
                Component formClone = (Component) replicator.createClone();
                if (formClone != null) {
                    formClone.setVisible(true);
                    setupDesignerSize(formClone);
                    componentLayer.setTopDesignComponent(formClone);
                    updateComponentLayer();
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
                    if ((metacont instanceof RADVisualContainer
                            || metacont instanceof RADMenuComponent)
                        && (prevType != FormModelEvent.COMPONENT_ADDED
                            || prevContainer != metacont))
                    {
                        replicator.updateAddedComponents(metacont);
                        updateDone = true;
                    }
                }
                else if (type == FormModelEvent.COMPONENT_REMOVED) {
                    RADComponent removed = ev.getComponent();

                    // if the top designed component (or some of its parents)
                    // was removed then whole designer view must be recreated
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
                {
                    componentLayer.setDesignerSize(getStoredDesignerSize());
                    updateDone = true;
                }

                prevType = type;
                prevContainer = metacont;
            }

            if (updateDone) {
                updateComponentLayer();
                getLayoutDesigner().externalSizeChangeHappened();
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
    
    /**
     * Action that aligns selected components in the specified direction.
     */
    private class AlignAction extends AbstractAction {
        // PENDING change to icons provided by Dusan
        private static final String ICON_BASE = "org/netbeans/modules/form/resources/align_"; // NOI18N
        /** Dimension to align in. */
        private int dimension;
        /** Requested alignment. */
        private int alignment;
        /** Group/Align action. */
        private boolean closed;
        
        /**
         * Creates action that aligns selected components in the specified direction.
         *
         * @param dimension dimension to align in.
         * @param alignment requested alignment.
         */
        AlignAction(int dimension, int alignment, boolean closed) {
            this.dimension = dimension;
            this.alignment = alignment;
            this.closed = closed;
            boolean horizontal = (dimension == LayoutConstants.HORIZONTAL);
            boolean leading = (alignment == LayoutConstants.LEADING);
            String code;
            if (alignment == LayoutConstants.CENTER) {
                code = (horizontal ? "ch" : "cv"); // NOI18N
            } else {
                code = (horizontal ? (leading ? "l" : "r") : (leading ? "u" : "d")); // NOI18N
            }
            String iconResource = ICON_BASE + code + ".gif"; // NOI18N
            putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage(iconResource)));
            putValue(Action.SHORT_DESCRIPTION, FormUtils.getBundleString("CTL_AlignAction_" + code)); // NOI18N
            setEnabled(false);
        }
        
        /**
         * Performs the alignment of selected components.
         *
         * @param e event that invoked the action.
         */
        public void actionPerformed(ActionEvent e) {
            align(closed, dimension, alignment);
        }
        
        public int getDimension() {
            return dimension;
        }
        
        public int getAlignment() {
            return alignment;
        }
        
    }    
    /**
     * Action that aligns selected components in the specified direction.
     */
    private class ResizabilityAction extends AbstractAction {
        // PENDING change to icons provided by Dusan
        private static final String ICON_BASE = "org/netbeans/modules/form/resources/resize_"; // NOI18N
        /** Dimension of resizability. */
        private int dimension;
        
        /**
         * Creates action that changes the resizability of the component.
         *
         * @param dimension dimension of the resizability
         */
        ResizabilityAction(int dimension) {
            this.dimension = dimension;
            String code = (dimension == LayoutConstants.HORIZONTAL) ? "h" : "v";
            String iconResource = ICON_BASE + code + ".gif"; // NOI18N
            putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage(iconResource)));
            putValue(Action.SHORT_DESCRIPTION, FormUtils.getBundleString("CTL_ResizeButton_" + code)); // NOI18N
            setEnabled(false);
        }
        
        /**
         * Performs the resizability change of selected components.
         *
         * @param e event that invoked the action.
         */
        public void actionPerformed(ActionEvent e) {
            FormModel formModel = getFormModel();
            LayoutModel layoutModel = formModel.getLayoutModel();
            Object layoutUndoMark = layoutModel.getChangeMark();
            javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
            LayoutDesigner layoutDesigner = getLayoutDesigner();
            Collection componentIds = componentIds();
            Set containers = new HashSet();
            Iterator iter = componentIds.iterator();
            while (iter.hasNext()) {
                String compId = (String)iter.next();
                LayoutComponent layoutComp = layoutModel.getLayoutComponent(compId);
                boolean resizing = (getResizabilityButtons()[dimension]).isSelected();
                if (layoutDesigner.isComponentResizing(layoutComp, dimension) != resizing) {
                    layoutDesigner.setComponentResizing(layoutComp, dimension, resizing);
                    RADVisualComponent comp = (RADVisualComponent)formModel.getMetaComponent(compId);
                    containers.add(comp.getParentContainer());
                }
            }
            iter = containers.iterator();
            while (iter.hasNext()) {
                formModel.fireContainerLayoutChanged((RADVisualContainer)iter.next(), null, null, null);
            }
            if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                formModel.addUndoableEdit(ue);
            }
        }
    }
    
    static class FormProxyLookup extends ProxyLookup {
        
        FormProxyLookup(Lookup[] lookups) {
            super(lookups);
        }
        
        Lookup[] getSubLookups() {
            return getLookups();
        }
        
        void setSubLookups(Lookup[] lookups) {
            setLookups(lookups);
        }
        
    }
    
}
