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

import java.io.*;
import java.util.*;
import java.awt.*;
import java.beans.*;
import java.text.MessageFormat;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.explorer.*;
import org.openide.explorer.propertysheet.*;
import org.openide.awt.SplittedPanel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.util.HelpCtx;
import org.openide.util.SharedClassObject; 

import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.palette.*;

/**
 * The ComponentInspector explorer
 **/

public class ComponentInspector extends ExplorerPanel implements Serializable
{
    private static TestAction testAction = (TestAction)
                       SharedClassObject.findObject(TestAction.class, true);

    private static ComponentInspectorAction inspectorAction =
        (ComponentInspectorAction)
            SharedClassObject.findObject(ComponentInspectorAction.class, true);

    /** The default width of the ComponentInspector */
    public static final int DEFAULT_INSPECTOR_WIDTH = 250;
    /** The default height of the ComponentInspector */
    public static final int DEFAULT_INSPECTOR_HEIGHT = 400;
    /** The default percents of the splitting of the ComponentInspector */
    public static final int DEFAULT_INSPECTOR_PERCENTS = 30;

    private static EmptyInspectorNode emptyInspectorNode;

    /** Default icon base for control panel. */
    private static final String EMPTY_INSPECTOR_ICON_BASE =
        "/org/netbeans/modules/form/resources/emptyInspector"; // NOI18N
    
    private static ResourceBundle formBundle = FormEditor.getFormBundle();
    
    /** Currently focused form or null if no form is opened/focused */
    private FormModel formModel;
    private boolean focusingOnModel = false;

    private SplittedPanel split;
    private PropertySheetView sheet;

    private static final java.net.URL iconURL = 
        ComponentInspector.class.getResource("/org/netbeans/modules/form/resources/inspector.gif"); // NOI18N

    /** The Inspector's icon */
    private final static Image inspectorIcon = Toolkit.getDefaultToolkit().getImage(iconURL);

    static final long serialVersionUID =4248268998485315927L;

    private static ComponentInspector instance;

    public static ComponentInspector getInstance() {
        if (instance == null)
            instance = new ComponentInspector();
        return instance;
    }
    
    private ComponentInspector() {
        ExplorerManager manager = getExplorerManager();
        emptyInspectorNode = new EmptyInspectorNode();
        manager.setRootContext(emptyInspectorNode);
        
        setLayout(new BorderLayout());
        
        createSplit();

        setIcon(inspectorIcon);
        setName(formBundle.getString("CTL_InspectorTitle"));

        manager.addPropertyChangeListener(new NodeSelectedListener());
    }

    private void createSplit() {
        split = new SplittedPanel();
        split.add(new BeanTreeView(), SplittedPanel.ADD_FIRST);
        split.add(sheet = new PropertySheetView(), SplittedPanel.ADD_SECOND);
        split.setSplitType(SplittedPanel.VERTICAL);
        split.setSplitPosition(DEFAULT_INSPECTOR_PERCENTS);
        sheet.setDisplayWritableOnly(
            FormEditor.getFormSettings().getDisplayWritableOnly());
        add(BorderLayout.CENTER, split);
    }

    class NodeSelectedListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent evt) {
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;
            if (formModel == null)
                return;
            
            FormDesigner designer = formModel.getFormDesigner();
            if (designer == null)
                return;
                        
            Node[] selectedNodes = getExplorerManager().getSelectedNodes();

            if (ComponentPalette.getDefault().getMode() ==
                PaletteAction.MODE_CONNECTION) {
                if (selectedNodes.length < 1)
                    return;
                
                RADComponentCookie cookie =
                    (RADComponentCookie) selectedNodes[0]
                    .getCookie(RADComponentCookie.class);
                
                try {
                    getExplorerManager().setSelectedNodes(new Node[0]);
                }
                catch (PropertyVetoException ex) {}
                
                if (cookie != null)
                    designer.connectBean(cookie.getRADComponent());
            }
            else if (!focusingOnModel) {
                designer.clearSelectionImpl();
                
                for (int i = 0; i < selectedNodes.length; i++) {
                    RADComponentCookie cookie =
                        (RADComponentCookie) selectedNodes[i].getCookie(RADComponentCookie.class);
                    if (cookie != null) {
                        designer.addComponentToSelectionImpl(
                            cookie.getRADComponent());
                    }
                }
            }
        }
    }

    public void open(Workspace workspace) {
        Workspace realWorkspace = TopManager.getDefault().getWindowManager()
            .getCurrentWorkspace();
        Workspace visualWorkspace = TopManager.getDefault().getWindowManager()
            .findWorkspace(FormEditor.GUI_EDITING_WORKSPACE_NAME);
        
        Mode ourMode = realWorkspace.findMode(this);
        if ((ourMode == null) && workspace.equals(visualWorkspace)) {
            // create new mode for CI and set the bounds properly
            ourMode = workspace.createMode("ComponentInspector",  //NOI18N
                                           formBundle.getString("CTL_InspectorTitle"), // NOI18N
                                           iconURL);
            Rectangle workingSpace = workspace.getBounds();
            ourMode.setBounds(new Rectangle(
                workingSpace.x +(workingSpace.width * 3 / 10),
                workingSpace.y,
                workingSpace.width * 2 / 10,
                workingSpace.height / 2));
            ourMode.dockInto(this);
        }
        super.open(workspace);
    }

    public HelpCtx getHelpCtx() {
        return getHelpCtx(getExplorerManager().getSelectedNodes(),
                          new HelpCtx(ComponentInspector.class));
    }

    /** This method focuses the ComponentInspector on given form.
     * @param formModel form to be focused on
     */
    public void focusForm(final FormModel formModel) {
        if (this.formModel != formModel)
            focusFormInAwtThread(formModel, 0);
    }

    /** This method focuses the ComponentInspector on given form.
     * @param formModel form to be focused on
     * @param visible true to open inspector, false to close
     */
    public void focusForm(final FormModel formModel, boolean visible) {
        if (this.formModel != formModel)
            focusFormInAwtThread(formModel, visible ? 1 : -1);
    }

    private void focusFormInAwtThread(final FormModel formModel, final int visibility) {
        if (java.awt.EventQueue.isDispatchThread()) {
            focusFormImpl(formModel, visibility);
        }
        else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    focusFormImpl(formModel, visibility);
                }
            });
        }
    }

    private void focusFormImpl(FormModel formModel, int visibility) {
        this.formModel = formModel;
        
        testAction.setFormModel(formModel);
        inspectorAction.setEnabled(formModel != null);

        if (formModel == null) {
            // swing memory leak workaround
            remove(split);
            createSplit();
            getExplorerManager().setRootContext(emptyInspectorNode);
        }
        else {
            Node formNode = formModel.getFormEditorSupport().getFormRootNode();
            // XXX how can it be null?
            if (formNode == null) {
                System.err.println("Warning: FormEditorSupport.getFormRootNode() returns null");
                getExplorerManager().setRootContext(emptyInspectorNode);
            }
            else {
                sheet.setDisplayWritableOnly(!formModel.isReadOnly()
                     && FormEditor.getFormSettings().getDisplayWritableOnly());

                focusingOnModel = true;
                getExplorerManager().setRootContext(formNode);
                focusingOnModel = false;
            }
        }
        updateTitle();

        if (visibility > 0) open();
        else if (visibility < 0) close();
    }

    protected void updateTitle() {
        String title;

        if (formModel == null)
            setName(formBundle.getString("CTL_InspectorTitle"));
        else
            setName(formModel.getFormDataObject().getName());
    }

    FormModel getFocusedForm() {
        return formModel;
    }

    void setSelectedNodes(Node[] nodes, FormModel model) throws PropertyVetoException {
        if (model == formModel) {
            getExplorerManager().setSelectedNodes(nodes);
        }
    }

    Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }

    /** Fixed preferred size, so as the inherited preferred size is too big */
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_INSPECTOR_WIDTH, DEFAULT_INSPECTOR_HEIGHT);
    }

    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    static class EmptyInspectorNode extends AbstractNode {
        public EmptyInspectorNode() {
            super(Children.LEAF);
            setIconBase(EMPTY_INSPECTOR_ICON_BASE);
        }

        public boolean canRename() {
            return false;
        }
    }

    final public static class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7424646018839457544L;
        public Object readResolve() {
            return ComponentInspector.getInstance();
        }
    }
}
