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
package org.netbeans.modules.bpel.properties.editors.controls;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.EditorLifeCycle;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.form.valid.Validator.Reason;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * This panel is a part of framework for showing Custom Property Editors.
 * The DialogDescriptor and DialogDisplayer are used to show dialogs.
 * This panel is a base class for root panels which are intended to be passed
 * to the DialogDescriptor. Generally it doesn't contain any leaf controls.
 * In most cases it contans other containers like tabbed pane or panel.
 * The leaf controls like text fields, combo-boxes, buttons are put to
 * separate panel which usually designed with Form Editor.
 * <p>
 * This class provide support of following features:
 * - form life cycle tracking
 * - binding of simple controls to Node.Property
 * <p>
 * There is an agreement that all classes derived from this should
 * have the name with suffix "CustomEditor".
 * <p>
 * Sometimes a derived Custom Editor class hasn't any specific functions.
 * To simplity coding the special reusable {@link SimpleCustomEditor} class was designd.
 *
 * @author nk160297
 */
public class AbstractCustomNodeEditor<T> extends JPanel
        implements CustomNodeEditor<T> {
    
    private static final long serialVersionUID = 1L;
    
    // This Validation State Manager intended to be used when anything changed
    // by the user.
    private DefaultValidStateManager fastValidationState;
    
    // This Validation State Manager intended to be used after the user
    // press Ok button.
    private transient DefaultValidStateManager finalValidationState;
    
    private EditingMode editingMode = EditingMode.NOT_SPECIFIED;
    
    private BpelNode<T> modelNode;
    
    public AbstractCustomNodeEditor(BpelNode<T> sourceNode) {
        this.modelNode = sourceNode;
        //
        this.fastValidationState = new DefaultValidStateManager();
        this.finalValidationState = new DefaultValidStateManager();
    }
    
    public void createContent() {
        this.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
        this.setLayout(new BorderLayout());
        // processChildrenControls(this, LifeCycleStage.CREATE);
    }
    
    public boolean initControls() {
        assert editingMode != EditingMode.NOT_SPECIFIED ;
        //
        try {
            // Do explisit initialization at first!!!
            // It's necessary to allow loading data models for combo-boxes
            processChildrenControls(this, LifeCycleStage.INIT);
            initSimpleControls();
            fastValidationState.clearReasons();
            fastValidationState.validateChildrenControls(this, true);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
    public boolean subscribeListeners() {
        try {
            processChildrenControls(this, LifeCycleStage.SUBSCRIBE);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
    public boolean unsubscribeListeners() {
        try {
            processChildrenControls(this, LifeCycleStage.UNSUBSCRIBE);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
    public boolean afterClose() {
        try {
            processChildrenControls(this, LifeCycleStage.AFTER_CLOSE);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
    public boolean revalidate(boolean fast) {
        if (fast) {
            fastValidationState.clearReasons();
            fastValidationState.validateChildrenControls(this, fast);
            return fastValidationState.isValid();
        } else {
            finalValidationState.clearReasons();
            finalValidationState.validateChildrenControls(this, fast);
            return finalValidationState.isValid();
        }
    }
    
    public boolean applyNewValues() throws Exception {
        applySimpleControlsValues();
        processChildrenControls(AbstractCustomNodeEditor.this,
                LifeCycleStage.APPLY);
        return true;
    }
    
    public BpelNode<T> getEditedNode() {
        return modelNode;
    }
    
    public T getEditedObject() {
        return modelNode.getReference();
    }
    
    public Lookup getLookup() {
        return modelNode.getLookup();
    }
    
    public boolean doValidateAndSave() {
        boolean isSuccessfull = false;
        //
        if (revalidate(false)) {
            try {
                applyNewValues();
                isSuccessfull = true;
            } catch (VetoException ex) {
                String message = ex.getMessage();
                UserNotification.showMessage(ex, message);
            } catch (InvocationTargetException ex) {
                Throwable targetEx = ex.getTargetException();
                if (targetEx instanceof VetoException) {
                    String message = targetEx.getMessage();
                    UserNotification.showMessage(targetEx, message);
                } else {
                    ErrorManager.getDefault().notify(ex);
                }
            } catch (Throwable ex) {
                ErrorManager.getDefault().notify(ex);
            }
            //
        } else {
            if (finalValidationState != null) {
                Reason reason = finalValidationState.getFistReason(null);
                if (reason != null) {
                    UserNotification.showMessage(reason.getText());
                }
            }
        }
        //
        return isSuccessfull;
    }
    
    //===================================================================
    // Registration of the simple components with not null names
    //===================================================================
    
    protected Map<PropertyType, JComponent> name2SimpleComponentMap =
            new HashMap<PropertyType, JComponent>();
    protected boolean isComponentsRegistered = false;
    
    protected void initSimpleControls() {
        // PropertyUtils.lookForPropertyByName()
        registerNamedSimpleComponents();
        //
        for (Node.PropertySet propSet : modelNode.getPropertySets()) {
            for (Node.Property prop : propSet.getProperties()) {
                //
                // Check if the source property has a name. If it isn't named then skip it.
                PropertyType propType = (PropertyType)prop.getValue(
                        Constants.PROPERTY_TYPE_ATTRIBUTE);
                Class propClass = prop.getValueType();
                //
                if (boolean.class.isAssignableFrom(propClass) ||
                        Boolean.class.isAssignableFrom(propClass)) {
                    //
                    // Process Boolean controls
                    JComponent comp = name2SimpleComponentMap.get(propType);
                    if(comp instanceof JCheckBox) {
                        try {
                            if (comp != null) {
                                Boolean propValue = (Boolean)prop.getValue();
                                ((JCheckBox)comp).setSelected(propValue);
                            }
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                } else if (TBoolean.class.isAssignableFrom(propClass)) {
                    //
                    // Process Boolean controls
                    JComponent comp = name2SimpleComponentMap.get(propType);
                    
                    if(comp instanceof JCheckBox) {
                        try {
                            if (comp != null) {
                                TBoolean propValue = (TBoolean)prop.getValue();
                                if (propValue == null) {
                                    propValue = TBoolean.INVALID;
                                }
                                switch (propValue) {
                                    case YES:
                                        ((JCheckBox)comp).setSelected(true);
                                        break;
                                    case NO:
                                        ((JCheckBox)comp).setSelected(false);
                                        break;
                                    default:
                                        ((JCheckBox)comp).setSelected(false);
                                        break;
                                }
                            }
                        } catch (IllegalAccessException ex) {
                            ErrorManager.getDefault().notify(ex);
                        } catch (InvocationTargetException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                } else if (QName.class.isAssignableFrom(propClass)) {
                    //
                    // Process String controls
                    JComponent comp = name2SimpleComponentMap.get(propType);
                    if(comp instanceof QNameIndicator) {
                        try {
                            if (comp != null) {
                                QName propValue = (QName)prop.getValue();
                                ((QNameIndicator)comp).setQName(propValue);
                            }
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    } else if(comp instanceof QNameComboChooser) {
                        try {
                            if (comp != null) {
                                QName propValue = (QName)prop.getValue();
                                ((QNameComboChooser)comp).setQName(propValue);
                            }
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                } else if (String.class.isAssignableFrom(propClass)) {
                    //
                    // Process String controls
                    JComponent comp = name2SimpleComponentMap.get(propType);
                    if(comp instanceof JTextComponent) {
                        try {
                            if (comp != null) {
                                String propValue = (String)prop.getValue();
                                ((JTextComponent)comp).setText(propValue);
                            }
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                }
            }
        }
    }
    
    protected void applySimpleControlsValues() throws Exception {
        registerNamedSimpleComponents();
        //
        for (Node.PropertySet propSet : modelNode.getPropertySets()) {
            for (Node.Property prop : propSet.getProperties()) {
                //
                // Check if the source property has a name. If it isn't named then skip it.
                PropertyType propType = (PropertyType)prop.getValue(
                        Constants.PROPERTY_TYPE_ATTRIBUTE);
                Class propClass = prop.getValueType();
                //
                if (String.class.isAssignableFrom(propClass)) {
                    //
                    // Process String controls
                    JComponent comp = name2SimpleComponentMap.get(propType);
                    if(comp instanceof JTextComponent) {
                        if (comp != null) {
                            String compValue = ((JTextComponent)comp).getText();
                            // compare the old value with the new one
                            Object oldValue = prop.getValue();
                            if (prop.canWrite() && !compValue.equals(oldValue)) {
                                prop.setValue(compValue);
                            }
                        }
                    }
                } else if (boolean.class.isAssignableFrom(propClass) ||
                        Boolean.class.isAssignableFrom(propClass)) {
                    //
                    // Process Boolean controls
                    JComponent comp = name2SimpleComponentMap.get(propType);
                    if(comp instanceof JCheckBox) {
                        if (comp != null) {
                            Boolean compValue = ((JCheckBox)comp).isSelected()
                            ? Boolean.TRUE : Boolean.FALSE;
                            if (prop.canWrite()) {
                                prop.setValue(compValue);
                            }
                        }
                    }
                } else if (TBoolean.class.isAssignableFrom(propClass)) {
                    //
                    // Process Boolean controls
                    JComponent comp = name2SimpleComponentMap.get(propType);
                    if(comp instanceof JCheckBox) {
                        if (comp != null) {
                            TBoolean compValue = ((JCheckBox)comp).isSelected()
                            ? TBoolean.YES : TBoolean.NO;
                            if (prop.canWrite()) {
                                prop.setValue(compValue);
                            }
                        }
                    }
                } else if (QName.class.isAssignableFrom(propClass)) {
                    //
                    // Process String controls
                    JComponent comp = name2SimpleComponentMap.get(propType);
                    if(comp instanceof QNameIndicator) {
                        if (comp != null) {
                            QName compValue = ((QNameIndicator)comp).getQName();
                            if (prop.canWrite()) {
                                prop.setValue(compValue);
                            }
                        }
                    } else if(comp instanceof QNameComboChooser) {
                        if (comp != null) {
                            QName compValue = ((QNameComboChooser)comp).getQName();
                            if (prop.canWrite()) {
                                prop.setValue(compValue);
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void registerNamedSimpleComponents() {
        if (isComponentsRegistered) return;
        registerNamedSimpleComponents(this);
        isComponentsRegistered = true;
    }
    
    protected void registerNamedSimpleComponents(Container parent) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JTextComponent ||
                    comp instanceof JCheckBox ||
                    comp instanceof JComboBox) {
                PropertyType propType = (PropertyType)((JComponent)comp).
                        getClientProperty(PROPERTY_BINDER);
                name2SimpleComponentMap.put(propType, (JComponent)comp);
            }
            if (comp instanceof Container) {
                registerNamedSimpleComponents((Container)comp);
            }
        }
    }
    
    //===================================================================
    
    protected void processChildrenControls(Container parent, LifeCycleStage stage)
    throws Exception {
        for (Component comp : parent.getComponents()) {
            boolean processComplete = false;
            if (comp instanceof EditorLifeCycle) {
                switch(stage) {
                    case CREATE:
                        ((EditorLifeCycle)comp).createContent();
                        processComplete = true; // always complete!
                        break;
                    case INIT:
                        processComplete = ((EditorLifeCycle)comp).initControls();
                        break;
                    case SUBSCRIBE:
                        processComplete =
                                ((EditorLifeCycle)comp).subscribeListeners();
                        break;
                    case UNSUBSCRIBE:
                        processComplete =
                                ((EditorLifeCycle)comp).unsubscribeListeners();
                        break;
                    case APPLY:
                        processComplete = ((EditorLifeCycle)comp).applyNewValues();
                        break;
                    case AFTER_CLOSE:
                        processComplete = ((EditorLifeCycle)comp).afterClose();
                        break;
                }
            }
            //
            switch(stage) {
                case VALIDATE_FINAL:
                    finalValidationState.validateChildrenControls(parent, false);
                    processComplete = true;
                    break;
                case VALIDATE_FAST:
                    fastValidationState.validateChildrenControls(parent, true);
                    processComplete = true;
                    break;
            }
            if (!processComplete && comp instanceof Container) {
                processChildrenControls((Container)comp, stage);
            }
        }
    }
    
    public ValidStateManager getValidStateManager(boolean isFast) {
        return isFast ? fastValidationState : finalValidationState;
    }
    
    public EditingMode getEditingMode() {
        return editingMode;
    }
    
    public void setEditingMode(EditingMode newValue) {
        // TODO Current implementation doesn't support dynamic switching
        // of the Editing Mode. It can be required in case of presence the
        // Apply button in the dialog button bar. In the case it will required
        // to add mode change notification so the nested components will be able
        // to reconfugure.
        editingMode = newValue;
    }
    
    public HelpCtx getHelpCtx() {
        String helpID = modelNode.getNodeType().getHelpId();
        return new HelpCtx(helpID);
        // return new HelpCtx(this.getClass());
    }
    
    public enum LifeCycleStage {
        CREATE, INIT, SUBSCRIBE, VALIDATE_FAST,
        VALIDATE_FINAL, APPLY, UNSUBSCRIBE, AFTER_CLOSE
    }
    
    protected void fireHelpContextChange() {
        SoaUtil.fireHelpContextChange(this, this.getHelpCtx());
    }
}
