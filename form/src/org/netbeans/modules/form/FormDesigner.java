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

/* $Id$ */

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

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
        if (workspace == null)
            workspace = TopManager.getDefault().getWindowManager().getCurrentWorkspace();

        Mode mode = workspace.findMode(getName());
        if (mode != null) { // try to reuse mode
            TopComponent[] comps = mode.getTopComponents();
            int i;
            for (i=0; i < comps.length; i++)
                if (comps[i].isOpened()) break;

            if (i == comps.length) // only closed top components
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

    Iterator getSelectedComponents() {
        return selectedComponents.iterator();
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
            if (parent != null
                && parent.getLayoutSupport()
                instanceof LayoutSupportArranging) {
                LayoutSupportArranging lsa =
                    (LayoutSupportArranging) parent.getLayoutSupport();
                lsa.selectComponent(metacomp);
            }
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
//            revalidate();
//            repaint();
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

    /** Tests whether top designed container is some parent of a component
     * (whether given component is in the tree under top design container).
     */
    boolean isInDesignedTree(RADVisualComponent metacomp) {
        RADVisualContainer parent = metacomp.getParentContainer();
        while (parent != null) {
            if (parent == topDesignContainer) return true;
            parent = parent.getParentContainer();
        }
        return false;
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
        }
    }

    private void repopulateComponentLayerImpl() {
        componentLayer.removeAll();
        metaCompToComp.clear();
        compToMetaComp.clear();

        try {
            Container cont = cloneTopContainerInstance();
            componentLayer.add(cont, BorderLayout.CENTER);

            metaCompToComp.put(topDesignContainer, cont);
            compToMetaComp.put(cont, topDesignContainer);

            walkVisualComps(cont, topDesignContainer);
            handleLayer.requestFocus();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Container cloneTopContainerInstance() {
        Object cont = topDesignContainer.getBeanInstance();

        if (cont instanceof JScrollPane || cont instanceof JTabbedPane
            || cont instanceof JSplitPane)
            return (Container) topDesignContainer.cloneBeanInstance();

        return new JPanel(); // copy relevant properties
    }

    private void walkVisualComps(Object bean, RADComponent comp)
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
        
                Class cl = c.getBeanClass();
                Object cb;

                if (cl == Dialog.class)
                    cb = new Dialog((Frame)null);
                else {
                    cb = c.cloneBeanInstance();//cl.newInstance();
                    if (cb instanceof Component)
                        if (!(cb instanceof JComponent))
                            FakePeerSupport.attachFakePeer((Component) cb);
                        else {
                            ((JComponent)cb).setRequestFocusEnabled(false);
                            ((JComponent)cb).setNextFocusableComponent((JComponent)cb);
                        }
                }

                metaCompToComp.put(c, cb);
                compToMetaComp.put(cb, c);
                
                if (comp instanceof RADVisualContainer
                    && bean instanceof Container
                    && c instanceof RADVisualComponent
                    && cb instanceof Component) {

                    addComponentToContainer((RADVisualContainer) comp,
                                            (Container) bean,
                                            (RADVisualComponent) c,
                                            (Component) cb);
                }
                walkVisualComps(cb, c);
            }

            if (comp instanceof RADVisualContainer) {
                LayoutSupport ls = ((RADVisualContainer)comp).getLayoutSupport();
                if (ls instanceof LayoutSupportArranging) {
                    ((LayoutSupportArranging)ls).arrangeContainer((Container)bean);
                }
            }
        }
    }

    static void setContainerLayout(Container cont, LayoutSupport laySup) {
        if (laySup != null) {
            LayoutManager lm = laySup.cloneLayoutInstance(cont);
            if (lm != null) cont.setLayout(lm);

            if (cont instanceof JTabbedPane)
                ((JTabbedPane)cont).setSelectedIndex(-1);
        }
    }

    static boolean isContainer(Object instance)  {
        BeanInfo info = BeanSupport.createBeanInfo(instance.getClass());

        if (info != null)  {
            BeanDescriptor desc = info.getBeanDescriptor();
            Boolean flag = (Boolean)desc.getValue("isContainer");
            if (flag != null)  {
                return flag.booleanValue();
            }
        }
        // The isContainer attribute is not found.
        if (instance instanceof Container)
            return true;
        else
            return false;
    }

    static void addComponentToContainer(RADVisualContainer radcontainer,
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
                    ex.printStackTrace();
                }
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
//              else if (dl instanceof DesignAbsoluteLayout) {
//                  DesignAbsoluteLayout.AbsoluteConstraintsDescription acd =
//                      (DesignAbsoluteLayout.AbsoluteConstraintsDescription) constrDesc;
        
                
//                  Point pos = acd.getPosition();
//                  Dimension size = acd.getSize();
//                  if (size.width <= 0 || size.height <= 0) {
//                      size = ((Component) radcomp.getBeanInstance()).getPreferredSize();
//                  }

//                  root.add(comp);
//                  comp.setBounds(pos.x, pos.y, size.width, size.height);
//              }
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
