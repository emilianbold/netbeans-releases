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
import org.openide.explorer.ExplorerPanel;
import org.openide.filesystems.*;

import org.netbeans.modules.form.*;
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
    private JLayeredPane layeredPane;

    private ComponentLayer componentLayer;
    private HandleLayer handleLayer;
    private InPlaceEditLayer textEditLayer;
    private FormDesignerPanel fdPanel;
    private RADProperty editedProperty;

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

    private VisualUpdater updater = new VisualUpdater();
    private boolean updateTaskPlaced;

    private final ArrayList selectedComponents = new ArrayList();

    private RADComponent connectionSource;
    private RADComponent connectionTarget;
    
    /** The icons for FormDesigner */
    private static String iconURL = "/org/netbeans/modules/form/resources/formDesigner.gif"; // NOI18N
    private static String icon32URL = "/org/netbeans/modules/form/resources/formDesigner32.gif"; // NOI18N
    
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
        super.writeExternal(out);
        out.writeObject(FormEditorSupport.getFormDataObject(formModel));
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object o = in.readObject();
        if (o instanceof FormDataObject) {
            FormEditorSupport formSupport = ((FormDataObject)o).getFormEditor();
            if (formSupport.loadForm()) {
//                FormModel model = formSupport.getFormModel();
//                model.setFormDesigner(this);
                setModel(formSupport.getFormModel());
                initialize();
                formSupport.setFormDesigner(this);
                ComponentInspector.getInstance().focusForm(formSupport);
            }
        }
    }

    public void open(Workspace workspace) {
        if (formModel == null)
            return;
        if (isOpened())
            return;
        
        if (workspace == null)
            workspace = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
        
        Mode mode = workspace.findMode("Form"); // NOI18N
        
        if (mode != null) {
            mode.dockInto(this);
        }

        super.open(workspace);
    }

    protected void componentActivated() {
        super.componentActivated();

        ComponentInspector ci = ComponentInspector.getInstance();
        if (ci.getFocusedForm() != formEditorSupport) {
            ComponentInspector.getInstance().focusForm(formEditorSupport);
            updateActivatedNodes();
        }

        FormEditor.actions.attach(ci.getExplorerManager());
        if (textEditLayer == null || !textEditLayer.isVisible())
            handleLayer.requestFocus();
        else
            textEditLayer.requestFocus();
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
    }
    
    private String generateModeName(FormDesigner fd) {
        FileObject fo = fd.getModel().getFormDataObject().getFormFile();
        String modeName = null;
        try {
            modeName = fo.getFileSystem().getDisplayName().replace(java.io.File.separatorChar, '.')
                      + "." + fo.getPackageNameExt('.', '.');
        }
        catch (FileStateInvalidException ex) {
            // nothing to do
        }
        return modeName;
    }
    
    /*
    private void updateFormsInOneWindow() {
        Workspace workspace = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
        String modeName = "Form";
        Mode mode = workspace.findMode(modeName);
        
        if (formSettings.getOpenFormsInOneWindow()) {
            if (mode == null) {
                mode = workspace.createMode(
                    modeName,
                    FormEditor.getFormBundle().getString("CTL_FormWindowTitle"), // NOI18N
                    null);                        
            }
            
            Object[] modes = workspace.getModes().toArray();
            for (int i=0; i<modes.length; i++) {
                if (((Mode)modes[i]).getName().endsWith(".form")) {
                    TopComponent[] comps = ((Mode)modes[i]).getTopComponents();
                    for (int j=0; j<comps.length; j++) {
                        if (comps[j].isOpened()) {
                            mode.dockInto(comps[j]);
                        }
                    }
                }
            }
        }
        else {
            TopComponent[] comps = mode.getTopComponents();
            
            for (int i=0; i < comps.length; i++) {
                if (comps[i].isOpened()) {
                    
                    modeName = generateModeName(((FormDesigner)comps[i]));
                    mode = workspace.findMode(modeName);
                    
                    if (mode == null) {
                        mode = workspace.createMode(
                                modeName,
                                FormEditor.getFormBundle().getString("CTL_FormWindowTitle"), // NOI18N
                                null);                        
                    }
                    
                    mode.dockInto(comps[i]);
                }
            }
        }
    }
     */

    ////////////////
    
    FormDesigner(FormModel formModel) {
        // instruct winsys to save state of this top component only if opened
        putClientProperty("PersistenceType", "OnlyOpened");
        
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
            RADComponent topRADComponent = formModel.getTopRADComponent();
            if (topRADComponent instanceof RADVisualFormContainer) {
                Dimension formSize = ((RADVisualFormContainer)topRADComponent).getFormSize();
                fdPanel.updatePanel(formSize);
            }
            else fdPanel.updatePanel(null);
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
        RADVisualContainer metacont = metacomp.getParentContainer();

        if (comp == null) { // visual component doesn't exist yet
            if (metacont != null) {
                LayoutSupportManager laysup = metacont.getLayoutSupport();
                if (laysup.supportsArranging())
                    laysup.selectComponent(metacont.getIndexOf(metacomp));
            }
            return;
        }

        if (comp.isShowing())
            return; // component is showing
        if (!isInDesignedTree(metacomp))
            return; // component is not in designer

        Component topComp = (Component) getComponent(topDesignComponent);
        if (!topComp.isShowing())
            return; // designer is not showing

        RADVisualComponent child = metacomp;

        while (metacont != null) {
            Container cont = (Container) getComponent(metacont);
            LayoutSupportManager laysup = metacont.getLayoutSupport();
            if (laysup.supportsArranging()) {
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
    RADVisualComponent getNextVisualComponent(RADVisualComponent comp,
                                              boolean forward) {
        if (comp == null)
            return null;
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
        placeUpdateTask(UpdateTask.ALL, null);
        updateName(formModel.getName());
    }

    void placeUpdateTask(int type, Object updateObj) {
        if (formModel == null)
            return;

        updater.addTask(type, updateObj);
        if (!updateTaskPlaced) {
            updateTaskPlaced = true;
            SwingUtilities.invokeLater(updater);
        }
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
                resetConnection();
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
        connectionSource = null;
        connectionTarget = null;
        handleLayer.repaint();
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

        boolean requireLayer = false; // if the original component cannot be used
        comp = comp.getParent();
        while (comp != null && comp.getClass() != ComponentLayer.class) {
            if (!JComponent.class.isAssignableFrom(comp.getClass())) {
                requireLayer = true;
                break;
            }
            comp = comp.getParent();
        }

        return InPlaceEditLayer.supportsEditingFor(metacomp.getBeanClass(),
                                                   requireLayer);
    }

    private void notifyCannotEditInPlace() {
        TopManager.getDefault().notify(
            new NotifyDescriptor.Message(
                FormEditor.getFormBundle().getString("MSG_ComponentNotShown"), // NOI18N
                NotifyDescriptor.WARNING_MESSAGE));
    }

    // -----------
    // innerclasses

    // Listener on FormModel - ensures visual updating by creating
    // update tasks (managed by VisualUpdater).
    class FormListener extends FormModelAdapter {

        public void containerLayoutChanged(FormModelEvent e) {
            placeUpdateTask(UpdateTask.LAYOUT, e.getContainer());
        }

        public void componentLayoutChanged(FormModelEvent e) {
            RADComponent metacomp = e.getComponent();
            if (metacomp instanceof RADVisualComponent) {
                placeUpdateTask(UpdateTask.LAYOUT,
                                metacomp.getParentComponent());
            }
        }

        public void componentAdded(FormModelEvent e) {
            RADComponent metacomp = e.getComponent();
            if (isInDesignedTree(metacomp))
                placeUpdateTask(UpdateTask.ADD, metacomp);
        }

        public void componentRemoved(FormModelEvent e) {
            RADComponent removed = e.getComponent();

            // test whether topDesignComponent or some of its parents
            // were not removed - then whole designer would be re-created
            if (removed instanceof RADVisualComponent
                && (removed == topDesignComponent
                    || removed.isParentComponent(topDesignComponent)))
            {
                resetTopDesignComponent(false);
                placeUpdateTask(UpdateTask.ALL, null);
            }
            else
                placeUpdateTask(UpdateTask.REMOVE, removed);

            if (isComponentSelected(removed))
                removeComponentFromSelection(removed);
        }

        public void componentsReordered(FormModelEvent e) {
            placeUpdateTask(UpdateTask.ORDER, e.getContainer());
        }

        public void componentPropertyChanged(FormModelEvent e) {
            placeUpdateTask(UpdateTask.PROPERTY, e.getComponentProperty());
        }
        
        public void syntheticPropertyChanged(FormModelEvent e) {
            if (RADVisualFormContainer.PROP_FORM_SIZE.equals(e.getPropertyName())) {
                Dimension formSize = (Dimension)e.getPropertyNewValue();
                fdPanel.updatePanel(formSize);
            }
        }
    }

    // --------

    // Visual updater - collects and performs update tasks that in turn call
    // individual update operations.
    class VisualUpdater implements Runnable {

        ArrayList updateTasks;

        public void run() {
            if (updateTaskPlaced && updateTasks != null) {
                updateTaskPlaced = false;
                performTasks();
            }
        }

        synchronized public void addTask(int type, Object param) {
            if (updateTasks == null)
                updateTasks = new ArrayList();
            updateTasks.add(new UpdateTask(type, param));
        }

        synchronized public void performTasks() {
            int count = updateTasks.size();
            if (count <= 0)
                return;

            // analyze updates
            boolean[] useless = new boolean[count];
            for (int i=count-1; i > 0; i--) {
                UpdateTask itask = (UpdateTask) updateTasks.get(i);
                for (int j=i-1; j >= 0; j--) {
                    UpdateTask jtask = (UpdateTask) updateTasks.get(j);
                    if (itask.cancelsPreviousTask(jtask))
                        useless[j] = true;
                }
            }

            // extract valid updates
            boolean lafBlock = false;
            final java.util.List tasks = new ArrayList(count);
            for (int i=0; i < count; i++)
                if (!useless[i]) {
                    UpdateTask task = (UpdateTask) updateTasks.get(i);
                    tasks.add(task);
                    if (task.type == UpdateTask.ALL
                            || task.type == UpdateTask.ADD)
                        lafBlock = true;
                }

            updateTasks = null;

            if (lafBlock) { // perform update with real LAF defaults
                try {
                    FormLAF.executeWithLookAndFeel(
                        UIManager.getLookAndFeel().getClass().getName(),
                        new Mutex.ExceptionAction () {
                            public Object run() throws Exception {
                                for (Iterator it=tasks.iterator(); it.hasNext(); )
                                    ((UpdateTask)it.next()).performUpdate();
                                return null;
                        }
                    });
                }
                catch (Exception ex) { // no exception should occur
                    ex.printStackTrace();
                }
            }
            else // perform the update directly, without real LAF defaults
                for (Iterator it=tasks.iterator(); it.hasNext(); )
                    ((UpdateTask)it.next()).performUpdate();

            revalidate();
            repaint();
        }
    }

    // -----------

    // Update task - is responsible for calling one update operation
    // on VisualReplicator.
    class UpdateTask {
        // types of update
        static final int ALL = 1; // re-create whole form
        static final int ORDER = 2; // reorder components
        static final int LAYOUT = 3; // update container layout
        static final int ADD = 4; // add component
        static final int REMOVE = 5; // remove component
        static final int PROPERTY = 6; // update property

        int type;
        Object param;

        UpdateTask(int type, Object param) {
            this.type = type;
            this.param = param;
        }

        void performUpdate() {
            switch (type) {
                case ALL:
                    componentLayer.removeAll();
                    replicator.setTopMetaComponent(topDesignComponent);
                    Component formClone = (Component) replicator.createClone();
                    if (formClone != null) {
                        formClone.setVisible(true);
                        componentLayer.add(formClone, BorderLayout.CENTER);
                    }
                    updateName(formModel.getName());
                    break;

                case ORDER:
                    replicator.reorderComponents((ComponentContainer) param);
                    break;

                case LAYOUT:
                    replicator.updateContainerLayout((RADVisualContainer) param);
                    break;

                case ADD:
                    replicator.addComponent((RADComponent) param);
                    break;

                case REMOVE:
                    replicator.removeComponent((RADComponent) param);
                    break;

                case PROPERTY:
                    replicator.updateComponentProperty((RADProperty) param);
                    break;
            }
        }

        boolean cancelsPreviousTask(UpdateTask prevTask) {
            if (type == ALL)
                return true;
            if (prevTask.type == ALL)
                return false;

            switch (type) {
                case ORDER:
                case LAYOUT:
                    if (prevTask.type == type)
                        return param == prevTask.param;
                    if (prevTask.type == ADD || prevTask.type == REMOVE)
                        return prevTask.param instanceof RADComponent
                                && ((RADComponent)prevTask.param)
                                        .getParentComponent() == param;
                    break;

                case ADD:
                    if (prevTask.type == ADD)
                        return param == prevTask.param;
                    break;

                case REMOVE:
                    RADComponent comp;
                    if (prevTask.param instanceof RADComponent)
                        comp = (RADComponent) prevTask.param;
                    else if (prevTask.param instanceof RADProperty)
                        comp = ((RADProperty)prevTask.param).getRADComponent();
                    else comp = null;

                    if (comp != null && comp == param)
                        return true;
                    break;
            }

            return false;
        }
    }
    
    
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
            setStatusText("FMT_MSG_RESIZING_FORMDESIGENR", // NOI18N
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
            
            if (resizingFinished) {
                RADComponent component = formDesigner.getModel().getTopRADComponent();
                if (component instanceof RADVisualFormContainer)
                    ((RADVisualFormContainer) component).setFormSize(new Dimension(w, h));
            }
            setStatusText("FMT_MSG_RESIZING_FORMDESIGENR", // NOI18N
                            new Object[] { new Integer(w).toString(), new Integer(h).toString() } );
        }
    }
}
