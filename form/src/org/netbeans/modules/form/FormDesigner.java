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
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.util.List;

import org.openide.*;
import org.openide.windows.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.explorer.ExplorerPanel;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.palette.*;
import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.layoutsupport.dedicated.*;

import org.netbeans.modules.form.compat2.border.*;
import org.netbeans.modules.form.fakepeer.FakePeerContainer;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 *
 * @author Tran Duc Trung
 */

public class FormDesigner extends TopComponent
{
    private JLayeredPane layeredPane;

    private ComponentLayer componentLayer;
    private HandleLayer handleLayer;
    private InPlaceEditLayer textEditLayer;
    private RADProperty editedProperty;

    private Runnable repopulateTask;
    private boolean repopulateTaskPlaced;

    private RADVisualContainer topDesignContainer;

    private JMenuBar formJMenuBar;
    private MenuBar formMenuBar;
    
    private FormModel formModel;
    private FormModelListener formModelListener;
    
    private final Map metaCompToComp = new HashMap();
    private final Map compToMetaComp = new HashMap();
    private final ArrayList selectedComponents = new ArrayList();

    private RADComponent connectionSource;
    private RADComponent connectionTarget;

    public FormDesigner() {
        this(null);
    }

    void initialize() {
        repopulateComponentLayer();
        
        // set menu bar
        Object menuVal = topDesignContainer.getAuxValue(
            RADVisualFormContainer.AUX_MENU_COMPONENT);
        if (menuVal != null && menuVal instanceof String) {
            ((RADVisualFormContainer)topDesignContainer)
                .setFormMenu((String)menuVal);
        }
    }
    
    //////
    
    public HelpCtx getHelpCtx() {
        return ExplorerPanel.getHelpCtx(getActivatedNodes(),
                                        new HelpCtx(this.getClass()));
    }

    public Dimension getPreferredSize() {
        return new Dimension(400, 300);
    }

    //
    //
    //
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(formModel.getFormDataObject());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object o = in.readObject();
        if (o instanceof FormDataObject) {
            FormEditorSupport formSupport = ((FormDataObject)o).getFormEditor();
            if (formSupport.loadForm()) {
                FormModel model = formSupport.getFormModel();
                model.setFormDesigner(this);
                setModel(model);
                initialize();
                ComponentInspector.getInstance().focusForm(model);
            }
        }
    }

    public void open(Workspace workspace) {
        if (formModel == null)
            return;
        
        if (workspace == null)
            workspace = TopManager.getDefault().getWindowManager().getCurrentWorkspace();

        String formName = "Form " + formModel.getFormDataObject().getName(); // NOI18N
        Mode mode = workspace.findMode(formName);

        if (mode != null) { // try to reuse mode
            TopComponent[] comps = mode.getTopComponents();
            int i;
            for (i=0; i < comps.length; i++)
                if (comps[i].isOpened()) break;

            if (i == comps.length) // all top components closed
                mode.dockInto(this);
            else mode = null;
        }

        if (mode == null) { // create new mode
            mode = workspace.createMode(
                     formName,
                     FormEditor.getFormBundle().getString("CTL_FormWindowTitle"), // NOI18N
                     null);
            mode.dockInto(this);
        }

        super.open(workspace);
    }

    protected void componentActivated() {
        super.componentActivated();

        ComponentInspector ci = ComponentInspector.getInstance();
        if (ci.getFocusedForm() != formModel) {
            ComponentInspector.getInstance().focusForm(formModel);
            updateActivatedNodes();
        }

        FormEditor.actions.attach(ci.getExplorerManager());
        handleLayer.requestFocus();
    }

    protected void componentDeactivated() {
        if (textEditLayer != null && textEditLayer.isVisible()) {
            textEditLayer.finishEditing(false);
        }
        FormEditor.actions.detach();
        resetConnection();
        super.componentDeactivated();
    }

    void updateActivatedNodes() {
        ComponentInspector ci = ComponentInspector.getInstance();
        if (ci.getFocusedForm() != formModel)
            return;

        Node[] selectedNodes = new Node[selectedComponents.size()];
        Iterator iter = selectedComponents.iterator();
        int i = 0;
        while (iter.hasNext()) {
            RADComponent metacomp = (RADComponent) iter.next();
            selectedNodes[i++] = metacomp.getNodeReference();
        }
        try {
            ci.setSelectedNodes(selectedNodes, formModel);
        }
        catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
            
        setActivatedNodes(selectedNodes);
    }

    void updateName() {
        String name = formModel.getFormDataObject().getName();
        if (!(topDesignContainer instanceof FormContainer))
            name += " / " + topDesignContainer.getName(); // NOI18N
        if (formModel.isReadOnly())
            name += " " + FormEditor.getFormBundle().getString("CTL_FormTitle_RO"); // NOI18N
        setName(name);
    }

    ////////////////
    
    FormDesigner(FormModel formModel) {
        formModelListener = new FormListener();
        
        componentLayer = new ComponentLayer();
        FakePeerContainer fakeContainer = new FakePeerContainer();
        fakeContainer.setLayout(new BorderLayout());
        fakeContainer.add(componentLayer, BorderLayout.CENTER);

        handleLayer = new HandleLayer(this);
        
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.add(fakeContainer, new Integer(1000));
        layeredPane.add(handleLayer, new Integer(1001));

        setLayout(new BorderLayout());
        add(layeredPane, BorderLayout.CENTER);

        setModel(formModel);
    }

    void setModel(FormModel m) {
        if (formModel != null)
            formModel.removeFormModelListener(formModelListener);
        formModel = m;
        
        if (formModel != null) {
            formModel.addFormModelListener(formModelListener);
            topDesignContainer = (RADVisualContainer) formModel.getTopRADComponent();
            handleLayer.setViewOnly(formModel.isReadOnly());
        }
    }

    FormModel getModel() {
        return formModel;
    }

    public Object getComponent(RADComponent metacomp) {
        return metaCompToComp.get(metacomp);
    }

    public RADComponent getMetaComponent(Object comp) {
        return (RADComponent) compToMetaComp.get(comp);
    }

    List getSelectedComponents() {
        return selectedComponents;
    }

    void setSelectedComponent(RADComponent metacomp) {
        clearSelectionImpl();
        addComponentToSelectionImpl(metacomp);
        updateActivatedNodes();
    }

    void addComponentToSelectionImpl(RADComponent metacomp) {
        if (metacomp == null) {
//            System.err.println("**** cannot add null to selection");
            Thread.dumpStack();
            return;
        }
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

        if (comp == null) { // visual component doesn't exist yet
            RADVisualContainer parent = metacomp.getParentContainer();
            LayoutSupport ls;
            if (parent != null && (ls = parent.getLayoutSupport())
                                  instanceof LayoutSupportArranging)
                ((LayoutSupportArranging)ls).selectComponent(metacomp);

            return;
        }

        if (comp.isShowing()) return; // component is showing
        if (!isInDesignedTree(metacomp)) return; // component is not in designer

        Component topComp = (Component) getComponent(topDesignContainer);
        if (!topComp.isShowing()) return; // designer is not showing

        RADVisualContainer parent = metacomp.getParentContainer();
        RADVisualComponent child = metacomp;
        Container parentComp;
        Component childComp = comp;

        while (parent != null) {
            parentComp = (Container) getComponent(parent);
            if (parent.getLayoutSupport() instanceof LayoutSupportArranging) {
                LayoutSupportArranging lsa =
                    (LayoutSupportArranging)parent.getLayoutSupport();
                lsa.selectComponent(child);
                lsa.arrangeContainer(parentComp);
            }
            if (parent == topDesignContainer || parentComp.isShowing()) break;
            child = parent;
            childComp = parentComp;
            parent = parent.getParentContainer();
        }
    }

    /** Finds out what component follows after currently selected component
     * when TAB (forward true) or Shift+TAB (forward false) is pressed. 
     * @returns the next or previous component for selection
     */
    RADVisualComponent getNextVisualComponent(boolean forward) {
        if (selectedComponents.size() != 1 
            || (!(selectedComponents.get(0) instanceof RADVisualComponent)))
            return null;

        return getNextVisualComponent((RADVisualComponent)selectedComponents.get(0), forward);
    }

    /** @returns the next or prevoius component to component comp
     */
    RADVisualComponent getNextVisualComponent(RADVisualComponent comp, boolean forward) {
        if (comp == null) return null;
        if (getComponent(comp) == null) return null;

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
            if (comp == topDesignContainer) return topDesignContainer;
            cont = comp.getParentContainer();
            if (cont == null) return null; // should not happen

            int i = cont.getIndexOf(comp);
            while (i >= 0) {
                subComps = cont.getSubComponents();
                if (i+1 < subComps.length)
                    return subComps[i+1];

                if (cont == topDesignContainer) break;
                comp = cont; // one level up
                cont = comp.getParentContainer();
                if (cont == null) return null; // should not happen
                i = cont.getIndexOf(comp);
            }

            return topDesignContainer;
            //(RADVisualComponent) formModel.getTopRADComponent();
        }
        else { // backward
            // take the previuos component
            if (comp != topDesignContainer) {
                cont = comp.getParentContainer();
                if (cont == null) return null; // should not happen
                int i = cont.getIndexOf(comp);
                if (i >= 0) { // should be always true
                    if (i == 0) return cont; // the opposite to the 1st forward step

                    subComps = cont.getSubComponents();
                    comp = subComps[i-1];
                }
                else comp = topDesignContainer; //(RADVisualComponent) formModel.getTopRADComponent();
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

    void connectBean(RADComponent metacomp) {
        if (connectionSource == null) {
            connectionSource = metacomp;
            handleLayer.repaint();
        }
        else {
            if (metacomp == connectionSource)
                return;
            connectionTarget = metacomp;
            handleLayer.repaint();
            createConnection(connectionSource, metacomp);
            connectionSource = null;
            connectionTarget = null;
            handleLayer.repaint();
            ComponentPalette.getDefault().setMode(PaletteAction.MODE_SELECTION);
        }
    }

    RADComponent getConnectionSource() {
        return connectionSource;
    }

    RADComponent getConnectionTarget() {
        return connectionTarget;
    }

    public void resetConnection() {
        connectionSource = null;
        connectionTarget = null;
        handleLayer.repaint();
    }
    
    class FormListener extends FormModelAdapter
    {
        public void formChanged(FormModelEvent e) {
            repopulateComponentLayer();
        }

        public void componentRemoved(FormModelEvent e) {
            RADComponent removed = e.getComponent();
            if (removed instanceof RADVisualContainer) {
                // test whether topDesignContainer or some of its parents
                // were not removed
                RADVisualContainer metacont = topDesignContainer;
                do {
                    if (metacont == removed) {
                        topDesignContainer = (RADVisualContainer)
                                             formModel.getTopRADComponent();
                        break;
                    }
                    metacont = metacont.getParentContainer();
                }
                while (metacont != null);
            }
        }
    }

    ComponentLayer getComponentLayer() {
        return componentLayer;
    }

    void setFormJMenuBar(JMenuBar menubar) {
        if (menubar == formJMenuBar)
            return;
        if (formJMenuBar != null)
            remove(formJMenuBar);
        formJMenuBar = menubar;
        if (formJMenuBar != null)
            add(formJMenuBar, BorderLayout.NORTH);
    }

    void setFormMenuBar(MenuBar menubar) {
        if (menubar == formMenuBar)
            return;
        if (formJMenuBar != null)
            remove(formJMenuBar);
        formMenuBar = menubar;
        if (menubar != null) {
            formJMenuBar = (JMenuBar) RADMenuItemComponent.findDesignTimeMenu(menubar);
            add(formJMenuBar, BorderLayout.NORTH);
        }
        else
            formJMenuBar = null;
    }

    public void setTopDesignContainer(RADVisualContainer container) {
        topDesignContainer = container;
        repopulateComponentLayer();
    }

    public RADVisualContainer getTopDesignContainer() {
        return topDesignContainer;
    }

    /** Tests whether top designed container is some parent of given component
     * (whether the component is in the tree under top designed container).
     */
    public boolean isInDesignedTree(RADVisualComponent metacomp) {
        while (metacomp != null) {
            if (metacomp == topDesignContainer) return true;
            metacomp = metacomp.getParentContainer();
        }
        return false;
    }

    public static Container createContainerView(final RADVisualContainer metacont,
                                                final Class contClass)
        throws Exception
    {
        return (Container) FormLAF.executeWithLookAndFeel(
            UIManager.getLookAndFeel().getClass().getName(),
            new Mutex.ExceptionAction () {
                public Object run() throws Exception {
                    Container container = createTopContainer(metacont,
                                                             contClass,
                                                             null);
                    walkVisualComps(container, metacont, null);
                    return container;
                }
            }
        );
    }

    void repopulateComponentLayer() {
        if (formModel != null && !repopulateTaskPlaced) {
            repopulateTaskPlaced = true;
            if (repopulateTask == null)
                repopulateTask = new Runnable() {
                    public void run() {
                        repopulateTaskPlaced = false;
                        repopulateComponentLayerImpl();
                        revalidate();
                        repaint();
                    }
                };
            SwingUtilities.invokeLater(repopulateTask);

            updateName();
        }
    }

    private void repopulateComponentLayerImpl() {
        componentLayer.removeAll();
        metaCompToComp.clear();
        compToMetaComp.clear();

        try {
            FormLAF.executeWithLookAndFeel(
                UIManager.getLookAndFeel().getClass().getName(),
                new Mutex.ExceptionAction () {
                    public Object run() throws Exception {
                        Container cont = createTopContainer(
                            topDesignContainer,
                            null,
                            new Class[] { Window.class, Applet.class, RootPaneContainer.class }
                        );
                        if (!(cont instanceof JComponent))
                            FakePeerSupport.attachFakePeer(cont);
                        componentLayer.add(cont, BorderLayout.CENTER);

                        RADProperty prop;
                        Object val;
                        RADComponent topcomp = formModel.getTopRADComponent();

                        prop = topcomp.getPropertyByName("background"); // NOI18N
                        if (prop != null) {
                            val = prop.getTargetValue();
                            if (val instanceof Color)
                                componentLayer.setBackground((Color) val);
                        }
                        prop = topcomp.getPropertyByName("foreground"); // NOI18N
                        if (prop != null) {
                            val = prop.getTargetValue();
                            if (val instanceof Color)
                                componentLayer.setForeground((Color) val);
                        }
                        prop = topcomp.getPropertyByName("font"); // NOI18N
                        if (prop != null) {
                            val = prop.getTargetValue();
                            if (val instanceof Font)
                                componentLayer.setFont((Font) val);
                        }
                        
                        metaCompToComp.put(topDesignContainer, cont);
                        compToMetaComp.put(cont, topDesignContainer);

                        walkVisualComps(cont, topDesignContainer, FormDesigner.this);
                        handleLayer.requestFocus();
                        return null;
                    }
                });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Container createTopContainer(RADVisualContainer metacont,
                                                Class requiredClass,
                                                Class[] forbiddenClasses)
        throws Exception
    {
        Class beanClass = metacont.getBeanClass();
        boolean beanClassForbidden = false;

        if (forbiddenClasses != null) {
            for (int i=0; i < forbiddenClasses.length; i++) {
                if (forbiddenClasses[i].isAssignableFrom(beanClass)) {
                    beanClassForbidden = true;
                    break;
                }
            }
        }

        if (!beanClassForbidden) {
            if (requiredClass == null
                    || requiredClass.isAssignableFrom(beanClass))
                return (Container) metacont.cloneBeanInstance();
        }
        else if (requiredClass == null) // required class not specified
            requiredClass = JComponent.class.isAssignableFrom(beanClass)
                            || RootPaneContainer.class.isAssignableFrom(beanClass)
                            || (!Window.class.isAssignableFrom(beanClass)
                                && !Panel.class.isAssignableFrom(beanClass)) ?
                JPanel.class : Panel.class;

        Container container = (Container)
            CreationFactory.createDefaultInstance(requiredClass);

        if (container instanceof RootPaneContainer
            && !RootPaneContainer.class.isAssignableFrom(beanClass) // Swing
            && !Window.class.isAssignableFrom(beanClass) // AWT
            && !Applet.class.isAssignableFrom(beanClass)) // AWT
        {
            Container contentCont = (Container) metacont.cloneBeanInstance();
            ((RootPaneContainer)container).setContentPane(contentCont);
        }
        else
            RADComponent.setProps(container, metacont.getAllBeanProperties());

        return container;
    }

    private static void walkVisualComps(Object bean,
                                        RADComponent comp,
                                        FormDesigner designer)
        throws Exception
    {
        if (comp instanceof ComponentContainer) {
            if (comp instanceof RADVisualContainer) {
                Container root =
                    ((RADVisualContainer) comp).getContainerDelegate(bean);
                LayoutSupport lsupp = ((RADVisualContainer)comp).getLayoutSupport();
                setContainerLayout(root, lsupp);
            }

            RADComponent[] children = ((ComponentContainer) comp).getSubBeans();
            for (int i = 0; i < children.length; i++) {
                RADComponent c = children[i];
                Object cb = c.cloneBeanInstance();

                if (designer != null) {
                    if (cb instanceof Component) {
                        if (!(cb instanceof JComponent))
                            FakePeerSupport.attachFakePeer((Component) cb);
                        else {
                            ((JComponent)cb).setRequestFocusEnabled(false);
                            ((JComponent)cb).setNextFocusableComponent((JComponent)cb);
                        }
                    }
                    if (cb instanceof java.beans.DesignMode) {
                        ((java.beans.DesignMode) cb).setDesignTime(true);
                    }

                    designer.metaCompToComp.put(c, cb);
                    designer.compToMetaComp.put(cb, c);
                }

                walkVisualComps(cb, c, designer);

                if (comp instanceof RADVisualContainer
                    && bean instanceof Container
                    && c instanceof RADVisualComponent
                    && cb instanceof Component) {

                    addComponentToContainer((RADVisualContainer) comp,
                                            (Container) bean,
                                            (RADVisualComponent) c,
                                            (Component) cb);
                }
            }

            if (comp instanceof RADVisualContainer) {
                LayoutSupport ls = ((RADVisualContainer)comp).getLayoutSupport();
                if (ls instanceof LayoutSupportArranging) {
                    ((LayoutSupportArranging)ls).arrangeContainer((Container)bean);
                }
            }
        }
    }

    private static void setContainerLayout(Container cont,
                                           LayoutSupport laySup) {
        if (laySup != null) {
            if (laySup.getClass() == NullLayoutSupport.class)
                cont.setLayout(null);
            else {
                LayoutManager lm = laySup.cloneLayoutInstance(cont);
                if (lm != null) cont.setLayout(lm);

//                if (cont instanceof JTabbedPane)
//                    ((JTabbedPane)cont).setSelectedIndex(-1);
            }
        }
    }

    private static void addComponentToContainer(RADVisualContainer radcontainer,
                                                Container container,
                                                RADVisualComponent radcomp,
                                                Component comp) {
        comp.setName(radcomp.getName());
    
        if (container instanceof JScrollPane) {
            ((JScrollPane)container).setViewportView(comp);
        }
        else if (container instanceof JSplitPane) {
            LayoutSupport.ConstraintsDesc desc =
                radcomp.getConstraintsDesc(JSplitPaneSupport.class);
            if (desc instanceof JSplitPaneSupport.SplitConstraintsDesc)
                container.add(comp, desc.getConstraintsObject());
        }
        else if (container instanceof JTabbedPane) {
            LayoutSupport.ConstraintsDesc desc =
                radcomp.getConstraintsDesc(JTabbedPaneSupport.class);
            if (desc instanceof JTabbedPaneSupport.TabConstraintsDesc) {
                try {
                    FormProperty titleProperty = (FormProperty)desc.getProperties()[0];
                    FormProperty iconProperty = (FormProperty)desc.getProperties()[1];
                    ((JTabbedPane)container).addTab(
                        (String) titleProperty.getRealValue(),
                        (Icon) iconProperty.getRealValue(),
                        comp);
                }
                catch (Exception ex) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        ex.printStackTrace();
                }
            }
        }
        else if (container instanceof JLayeredPane) {
            LayoutSupport.ConstraintsDesc desc =
                    radcomp.getConstraintsDesc(JLayeredPaneSupport.class);
            if (desc instanceof JLayeredPaneSupport.LayeredConstraintsDesc) {
                container.add(comp, desc.getConstraintsObject());
                Rectangle bounds =
                    ((JLayeredPaneSupport.LayeredConstraintsDesc)desc)
                        .getBounds();
                if (bounds.width == -1 || bounds.height == -1) {
                    Dimension pref = comp.isDisplayable() ?
                                     comp.getPreferredSize() :
                                     radcomp.getComponent().getPreferredSize();
                    if (bounds.width == -1)
                        bounds.width = pref.width;
                    if (bounds.height == -1)
                        bounds.height = pref.height;
                }
                comp.setBounds(bounds);
            }
        }
        else {//if (isContainer(container))
            LayoutSupport layoutSupp = radcontainer.getLayoutSupport();
            if (layoutSupp == null) { // this should not happen
                System.out.println("FormDesigner.addComponentToContainer - LayoutSupport is null in container: "+radcontainer.getName());
                return;
            }
            Container root = radcontainer.getContainerDelegate(container);

            Object constr = null;
            LayoutSupport.ConstraintsDesc constrDesc =
                                            layoutSupp.getConstraints(radcomp);
            if (constrDesc != null)
                constr = constrDesc.getConstraintsObject();
      
            if (root.getLayout() != null) {
                if (null == constr)
                    root.add(comp);
                else
                    root.add(comp, constr);
            }
            else if (constrDesc instanceof AbsoluteLayoutSupport.AbsoluteConstraintsDesc) {
                // null layout
                root.add(comp);
                Rectangle bounds =
                    ((AbsoluteLayoutSupport.AbsoluteConstraintsDesc)constrDesc)
                        .getBounds();
                if (bounds.width == -1 || bounds.height == -1) {
                    Dimension pref = comp.isDisplayable() ?
                                     comp.getPreferredSize() :
                                     radcomp.getComponent().getPreferredSize();
                    if (bounds.width == -1)
                        bounds.width = pref.width;
                    if (bounds.height == -1)
                        bounds.height = pref.height;
                }
                comp.setBounds(bounds);
            }
        }
    }

    // ------------------
    // beans connection

    private void createConnection(RADComponent source, RADComponent target) {
        ConnectionWizard cw = new ConnectionWizard(formModel, source,target);

        if (cw.show()) {
            String bodyText = cw.getGeneratedCode();
            Event event = cw.getSelectedEvent();
            String eventName = cw.getEventName();
            EventHandler handler = null;

            for (java.util.Iterator iter = event.getHandlers().iterator(); iter.hasNext(); ) {
                EventHandler eh = (EventHandler) iter.next();
                if (eh.getName().equals(eventName)) {
                    handler = eh;
                    break;
                }
            }
            if (handler == null) { // new handler
                formModel.getFormEventHandlers().addEventHandler(event, eventName, bodyText);
                formModel.fireFormChanged();
            } else {
                handler.setHandlerText(bodyText);
            }
            event.gotoEventHandler(eventName);
        }
    }

    // -----------------
    // in-place editing

    public void startInPlaceEditing(RADComponent metacomp) {
        if (formModel.isReadOnly()) return;
        if (!isEditableInPlace(metacomp)) return;
        if (textEditLayer != null && textEditLayer.isVisible()) return;

        Component comp = (Component) getComponent(metacomp);
        if (comp == null) { // component is not visible
            notifyCannotEditInPlace();
            return;
        }

        RADProperty property = metacomp.getPropertyByName("text"); // NOI18N
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
//        layeredPane.remove(textEditLayer);
//        layeredPane.add(handleLayer, new Integer(1001));
        layeredPane.revalidate();
        layeredPane.repaint();
        handleLayer.requestFocus();
        editedProperty = null;
    }

    public static boolean isEditableInPlace(RADComponent metacomp) {
        Object bean = metacomp.getBeanInstance();
        return InPlaceEditLayer.supportsEditingFor(bean.getClass());
    }

    private void notifyCannotEditInPlace() {
        TopManager.getDefault().notify(
            new NotifyDescriptor.Message(
                FormEditor.getFormBundle().getString("MSG_ComponentNotShown"),
                NotifyDescriptor.WARNING_MESSAGE));
    }
}
