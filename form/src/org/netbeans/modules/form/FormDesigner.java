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
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.beans.*;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.layout.*;

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
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.netbeans.modules.form.layoutdesign.*;

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
    private AlignmentPalette alignmentPalette;

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
                    Node[] nodes = (Node[])evt.getNewValue();
                    explorerManager.setSelectedNodes(nodes);
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
        componentLayer.setDesignerSize(getStoredDesignerSize());

        layoutDesigner = new LayoutDesigner(formModel.getLayoutModel(),
                                            new LayoutMapper());
        
        updateWholeDesigner();
        
        // not very nice hack - it's better FormEditorSupport has its
        // listener registered after FormDesigner
        formEditor.reinstallListener();
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

    LayoutDesigner getLayoutDesigner() {
        return layoutDesigner;
    }
    
    FormEditor getFormEditor() {
        return formEditor;
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

    void updateComponentLayer() {
        componentLayer.revalidate();

        // after the components are layed out, sync the layout designer
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (getLayoutDesigner().updateCurrentState()) {
                    formModel.fireFormChanged(); // hack: to regenerate code once again
                }
                updateAlignmentPalette();
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
        updateAlignmentPalette();
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
        updateAlignmentPalette();
    }

    void repaintSelection() {
        handleLayer.repaint();
    }

    private void updateAlignmentPalette() {
        if (!FormEditor.isNaturalLayoutEnabled()) return;
        if (alignmentPalette == null) {
            alignmentPalette = new AlignmentPalette();
            handleLayer.add(alignmentPalette);
            Rectangle hBounds = handleLayer.getBounds();
            Dimension size = alignmentPalette.getPreferredSize();
            alignmentPalette.setBounds(hBounds.x + hBounds.width - size.width - 10,
                hBounds.y + hBounds.height - size.height - 10, size.width, size.height);            
        }
        alignmentPalette.updateState();
        alignmentPalette.setVisible(getDesignerMode() == MODE_SELECT);
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
        Collection selectedIds = selectedLayoutComponentIds();
        if (selectedIds.size() < 2) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                    FormUtils.getBundleString("MSG_AlignComponentCount"), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        RADComponent parent = commonParent(selectedIds);
        if (parent == null) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                    FormUtils.getBundleString("MSG_AlignCommonParent"), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
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
     * @return <code>Collection</code> of <code>Action</code> objects.
     */
    Collection getDesignerActions() {
        List actions = new LinkedList();
        actions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.LEADING));
        //actions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.CENTER));
        actions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.TRAILING));
        actions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.LEADING));
        //actions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.CENTER));
        actions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.TRAILING));
        return actions;
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

        private Padding padding;

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
                return 6; // default distance between components

            assert dimension == HORIZONTAL || dimension == VERTICAL;
            assert comp2Alignment == LEADING || comp2Alignment == TRAILING;
            assert paddingType == PADDING_RELATED || paddingType == PADDING_UNRELATED;

            int type = paddingType == PADDING_RELATED ?
                       Padding.RELATED : Padding.UNRELATED;
            int position = 0;
            if (dimension == HORIZONTAL) {
                position = comp2Alignment == LEADING ?
                           SwingConstants.EAST : SwingConstants.WEST;
            }
            else {
                position = comp2Alignment == LEADING ?
                           SwingConstants.SOUTH : SwingConstants.NORTH;
            }

            return getPadding().getPadding(comp1, comp2, position, type);
        }

        public int getPreferredPaddingInParent(String parentId,
                                               String compId,
                                               int dimension,
                                               int compAlignment)
        {
            JComponent comp = null;
            Component parent = getVisualComponent(parentId, true, false);
            if (parent != null) {
                RADVisualContainer metacont = (RADVisualContainer)
                                              getMetaComponent(parentId);
                parent = metacont.getContainerDelegate(parent);
                if (parent instanceof JComponent) {
                    comp = (JComponent) getVisualComponent(compId, true, true);
                }
                else {
                    parent = null;
                }
            }
            if (parent == null || comp == null)
                return 12; // default distance from parent border

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
            return getPadding().getPaddingRelativeToParent((JComponent)parent, comp,
                                                           alignment);
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

        private Padding getPadding() {
            if (padding == null)
                padding = Padding.getSharedInstance();
            return padding;
        }
    }

    // --------

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
                    if (metacont instanceof RADVisualContainer
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
//                componentLayer.revalidate();
//                repaintSelection();
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
        
        /**
         * Creates action that aligns selected components in the specified direction.
         *
         * @param dimension dimension to align in.
         * @param alignment requested alignment.
         */
        AlignAction(int dimension, int alignment) {
            this.dimension = dimension;
            this.alignment = alignment;
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
        }
        
        /**
         * Performs the alignment of selected components.
         *
         * @param e event that invoked the action.
         */
        public void actionPerformed(ActionEvent e) {
            boolean closed = ((e.getModifiers() & ActionEvent.ALT_MASK) != 0);
            align(closed, dimension, alignment);
        }
        
    }
    
    /**
     * Alignment Palette.
     *
     * @author Jan Stola
     */
    public class AlignmentPalette extends JPanel implements ActionListener {
        private static final int AP_WIDTH = 46;
        private static final int AP_HEIGHT = 31;
        private static final int AP_BORDER = 8;
        private static final int AP_TITLE = 6;
        private AlignmentButton[] buttons;

        public AlignmentPalette() {
            setLayout(null);
            setOpaque(false);
            setCursor(Cursor.getDefaultCursor());
            setPreferredSize(new Dimension(AP_WIDTH + 1, AP_TITLE + AP_HEIGHT + 1));
            buttons = new AlignmentButton[6];
            addButton(buttons[0] = new AlignmentButton(SwingConstants.LEFT));
            addButton(buttons[1] = new AlignmentButton(SwingConstants.RIGHT));
            addButton(buttons[2] = new AlignmentButton(SwingConstants.TOP));
            addButton(buttons[3] = new AlignmentButton(SwingConstants.BOTTOM));
            addButton(buttons[4] = new AlignmentButton(true));
            addButton(buttons[5] = new AlignmentButton(false));
            JLabel title = new JLabel();
            title.setBounds(AP_BORDER/2, 0, AP_WIDTH - AP_BORDER, AP_TITLE + 1);
            title.setOpaque(true);
            title.setBackground(new Color(216, 226, 231));
            title.setBorder(BorderFactory.createLineBorder(new Color(125, 155, 184)));
            add(title);
            MouseInputAdapter listener = createListener();
            title.addMouseListener(listener);
            title.addMouseMotionListener(listener);
        }

        private void addButton(AlignmentButton button) {
            add(button);
            button.addActionListener(this);
        }
        
        void updateState() {
            Collection componentIds = componentIds();
            LayoutModel layoutModel = getFormModel().getLayoutModel();
            LayoutDesigner layoutDesigner = getLayoutDesigner();
            Iterator iter = componentIds.iterator();
            boolean matchAlignment[] = new boolean[4];
            boolean cannotChangeTo[] = new boolean[4];
            boolean resizable[] = new boolean[2];
            boolean nonResizable[] = new boolean[2];
            while (iter.hasNext()) {
                String id = (String)iter.next();
                LayoutComponent comp = layoutModel.getLayoutComponent(id);
                int[][] alignment = new int[][] {
                    layoutDesigner.getAdjustableComponentAlignment(comp, LayoutConstants.HORIZONTAL),
                    layoutDesigner.getAdjustableComponentAlignment(comp, LayoutConstants.VERTICAL)};
                for (int i=0; i<4; i++) {
                    if ((alignment[i/2][1] & (1 << i%2)) == 0) { // the alignment cannot be changed
                        cannotChangeTo[i] = true;
                    }
                    if (alignment[i/2][0] != -1) { 
                        matchAlignment[i] = matchAlignment[i] || (alignment[i/2][0] == i%2);
                    }
                }
                for (int i=4; i<6; i++) {
                    if (layoutDesigner.isComponentResizing(comp,
                        (i == 4) ? LayoutConstants.HORIZONTAL : LayoutConstants.VERTICAL)) {
                        resizable[i-4] = true;
                    } else {
                        nonResizable[i-4] = true;
                    }
                }
            }
            for (int i=0; i<6; i++) {
                boolean match;
                boolean miss;
                if (i < 4) {
                    match = matchAlignment[i];
                    miss = matchAlignment[2*(i/2) + 1 - i%2];
                } else {
                    match = resizable[i-4];
                    miss = nonResizable[i-4];
                }
                buttons[i].setEnabled((match || miss) && ((i > 3) || !cannotChangeTo[i]));
                buttons[i].setSelected(!miss && match);
                buttons[i].setPaintDisabledIcon(match && miss);
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            int index = Arrays.asList(buttons).indexOf(e.getSource());
            assert (index != -1);
            AbstractButton button = (AbstractButton)e.getSource();
            if (!button.isSelected() && (index < 4)) {
                // Alignment buttons should not be unselected
                button.setSelected(true);
                return;
            }
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
                boolean changed = false;
                if (index < 4) {
                    int[] alignment = layoutDesigner.getAdjustableComponentAlignment(layoutComp, index/2);
                    if (((alignment[1] & (1 << index%2)) != 0) && (alignment[0] != index%2)) {
                        layoutDesigner.adjustComponentAlignment(layoutComp, index/2, index%2);
                        changed = true;
                    }
                } else {
                    boolean resizing = button.isSelected();
                    int dimension = (index-4)%2;
                    if (layoutDesigner.isComponentResizing(layoutComp, dimension) != resizing) {
                        layoutDesigner.setComponentResizing(layoutComp, dimension, resizing);
                        changed = true;
                    }
                }
                if (changed) {
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

        private MouseInputAdapter createListener() {
            return new MouseInputAdapter() {
                private Point lastPoint;

                public void mousePressed(MouseEvent e) {
                    lastPoint = pointFromComponentToHandleLayer(e.getPoint(), (Component)e.getSource());
                }

                public void mouseReleased(MouseEvent e) {
                    lastPoint = null;
                }

                public void mouseDragged(MouseEvent e) {
                    Point p = pointFromComponentToHandleLayer(e.getPoint(), (Component)e.getSource());
                    if (handleLayer.contains(p)) {
                        Rectangle bounds = AlignmentPalette.this.getBounds();
                        bounds.translate(p.x-lastPoint.x, p.y-lastPoint.y);
                        AlignmentPalette.this.setBounds(bounds);
                        lastPoint = p;
                    }
                }
            };
        }

        private class AlignmentButton extends JToggleButton {
            private Area border;
            private Area boundary;
            private Area arrow;
            private boolean paintDisabledIcon;

            public AlignmentButton(boolean horizontal) {
                setBorder(null);
                setBounds(AP_BORDER + 15*(horizontal ? 0 : 1), AP_BORDER + AP_TITLE, 15, 15);
                border = new Area(new Rectangle(0, 0, 15, 15));
                setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/form/resources/spring_" // NOI18N
                    + (horizontal ? "h" : "v") + ".gif"))); // NOI18N
            }

            public AlignmentButton(int alignment) {
                setBorder(null);
                setOpaque(false);
                int[] x = null;
                int[] y = null;
                switch (alignment) {
                    case SwingConstants.TOP:
                        setBounds(0, AP_TITLE, AP_WIDTH+1, AP_BORDER+1);
                        x = new int[] {0, AP_WIDTH, AP_WIDTH - AP_BORDER, AP_BORDER};
                        y = new int[] {0, 0, AP_BORDER, AP_BORDER};
                        arrow = new Area(new Polygon(new int[] {6, 0, 3}, new int[] {4, 4, 1}, 3));
                        break;
                    case SwingConstants.BOTTOM:
                        setBounds(0, AP_TITLE + AP_HEIGHT - AP_BORDER, AP_WIDTH+1, AP_BORDER+1);
                        x = new int[] {0, AP_WIDTH, AP_WIDTH - AP_BORDER, AP_BORDER};
                        y = new int[] {AP_HEIGHT, AP_HEIGHT, AP_HEIGHT - AP_BORDER, AP_HEIGHT - AP_BORDER};
                        arrow = new Area(new Polygon(new int[] {0, 6, 3}, new int[] {2, 2, 5}, 3));
                        break;
                    case SwingConstants.LEFT:                    
                        setBounds(0, AP_TITLE, AP_BORDER+1, AP_HEIGHT+1);
                        x = new int[] {0, 0, AP_BORDER, AP_BORDER};
                        y = new int[] {0, AP_HEIGHT, AP_HEIGHT - AP_BORDER, AP_BORDER};
                        arrow = new Area(new Polygon(new int[] {4, 4, 1}, new int[] {5, -1, 2}, 3));
                        break;
                    case SwingConstants.RIGHT:
                        setBounds(AP_WIDTH - AP_BORDER, AP_TITLE, AP_BORDER+1, AP_HEIGHT+1);
                        x = new int[] {AP_WIDTH, AP_WIDTH, AP_WIDTH - AP_BORDER, AP_WIDTH - AP_BORDER};
                        y = new int[] {0, AP_HEIGHT, AP_HEIGHT - AP_BORDER, AP_BORDER};
                        arrow = new Area(new Polygon(new int[] {2, 2, 5}, new int[] {5, -1, 2}, 3));
                        break;
                    default: assert false;
                }
                Shape s1 = new RoundRectangle2D.Double(0, AP_TITLE, AP_WIDTH, AP_HEIGHT, 2*AP_BORDER, 2*AP_BORDER);
                Shape s2 = new Polygon(x, y, 4);                
                Area a = new Area(s2);
                a.transform(AffineTransform.getTranslateInstance(0, AP_TITLE));
                border = new Area(s1);
                boundary = new Area(s1);
                border.intersect(a);
                Point loc = getLocation();
                AffineTransform tr = AffineTransform.getTranslateInstance(-loc.x, -loc.y);
                border.transform(tr);
                boundary.transform(tr);
                Dimension dim = getSize();
                arrow.transform(AffineTransform.getTranslateInstance(dim.width/2-3, dim.height/2-3));
            }
            
            protected void setPaintDisabledIcon(boolean paintDisabledIcon) {
                this.paintDisabledIcon = paintDisabledIcon;
            }

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                ButtonModel model = getModel();
                if (model.isSelected() || (model.isArmed() && model.isPressed())) {
                    g2.setColor(new Color(216, 226, 231));
                } else {
                    g2.setColor(new Color(241, 241, 241));
                }
                g2.fill(border);
                g.setColor(new Color(173, 192, 206));
                g2.draw(border);
                if (boundary != null) {
                    g.setColor(new Color(125, 155, 184));
                    g2.draw(boundary);
                }
                Icon icon = getIcon();
                if (icon != null) {
                    if (paintDisabledIcon || !model.isEnabled()) {
                        icon = getDisabledIcon();
                    }
                    icon.paintIcon(null, g, (getWidth() - icon.getIconWidth())/2, (getHeight() - icon.getIconHeight())/2);
                }
                if (arrow != null) {
                    if (model.isEnabled() && !paintDisabledIcon) {
                        g.setColor(new Color(31, 82, 127));
                    } else {
                        g.setColor(Color.lightGray);
                    }
                    g2.fill(arrow);
                    g2.draw(arrow);
                }
            }

            public boolean contains(int x, int y) {
                return (border == null) ? true : border.contains(x, y);
            }
        }
    }
    
}
