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

package org.netbeans.modules.xml.schema.abe.wizard;

import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.axi.SchemaGeneratorFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class SchemaTransformPatternSelection implements WizardDescriptor.Panel, ChangeListener {
    private static final long serialVersionUID = 1L;
    
    SchemaGenerator.Pattern inferedPattern;
    
    RequestProcessor.Task transformTask = null;
    
    //a cache to store previous hints
    HashMap<SchemaGenerator.Pattern,SchemaGeneratorFactory.TransformHint> hintMap =
            new HashMap<SchemaGenerator.Pattern,SchemaGeneratorFactory.TransformHint>();
    
    List<Element> ges = null;
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SchemaTransformPatternSelectionUI component;
    
    private WizardDescriptor wizard = null;
    
    public SchemaTransformPatternSelection(SchemaGenerator.Pattern inferedPattern) {
        this.inferedPattern = inferedPattern;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new SchemaTransformPatternSelectionUI(inferedPattern);
            component.addChangeListener(this);
            
            //Error or Warnings that need to be shown on first time of
            //invocation is not feasible due to NB code WizardDescriptor.java:548
            //ie., wizardPanel is not yet initialized. So workaround is to try
            //invoking isValid() after 200 msec.
            transformTask = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    try {
                        isValid();
                    } catch(Throwable th) {
                        transformTask.schedule(300);
                    }
                }
            });
            transformTask.schedule(200);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(SchemaTransformPatternSelection.class); //NOI18n
    }
    
    public boolean isValid() {
        SchemaModel sm = (SchemaModel)
        wizard.getProperty(SchemaTransformWizard.SCHEMA_MODEL_KEY);
        SchemaGenerator.Pattern inferedPattern = (SchemaGenerator.Pattern)
        wizard.getProperty(SchemaTransformWizard.INFERED_DESIGN_PATTERN_KEY);
        SchemaGenerator.Pattern selectedPattern = (SchemaGenerator.Pattern)
        wizard.getProperty(SchemaTransformWizard.SELECTED_DESIGN_PATTERN_KEY);
        if(inferedPattern != null && selectedPattern != null) {
            SchemaGeneratorFactory.TransformHint hint = 
                    SchemaGeneratorFactory.TransformHint.OK;
            try {
                if(ges == null) //cache this for later use in this panel
                    ges = SchemaGeneratorFactory.getDefault().
                            findMasterGlobalElements(
                                AXIModelFactory.getDefault().getModel(sm));
                hint = canTransformSchema(sm, inferedPattern, selectedPattern, ges);
            }
            catch(Throwable th) {
                hint = SchemaGeneratorFactory.TransformHint.INVALID_SCHEMA;
            }
            if(hint != SchemaGeneratorFactory.TransformHint.OK) {
                if(inferedPattern == selectedPattern ||
                        hint == SchemaGeneratorFactory.TransformHint.INVALID_SCHEMA ||
                        hint == SchemaGeneratorFactory.TransformHint.NO_GLOBAL_ELEMENTS ||
                        hint == SchemaGeneratorFactory.TransformHint.
                        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS||
                        hint == SchemaGeneratorFactory.TransformHint.
                        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ATTRIBUTES||
                        hint == SchemaGeneratorFactory.TransformHint.
                        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS_AND_ATTRIBUTES||
                        hint == SchemaGeneratorFactory.TransformHint.
                        GLOBAL_ELEMENTS_HAVE_NO_GRAND_CHILDREN) {
                    setErrorMessage("MSG_SchemaTransform_Error_"+hint.toString(),
                            new Object[]{NbBundle.getMessage(
                                    SchemaTransformPatternSelectionUI.class,
                                    "LBL_SchemaTransform_"+selectedPattern.toString())});
                    return false;
                } else if(hint == SchemaGeneratorFactory.TransformHint.
                        CANNOT_REMOVE_GLOBAL_ELEMENTS) {
                    SchemaGenerator.Pattern other = SchemaGenerator.Pattern.SALAMI_SLICE;
                    if(inferedPattern == SchemaGenerator.Pattern.SALAMI_SLICE)
                        other = SchemaGenerator.Pattern.GARDEN_OF_EDEN;
                    setErrorMessage("MSG_SchemaTransform_Error_"+hint.toString(),
                            new Object[]{NbBundle.getMessage(
                                    SchemaTransformPatternSelectionUI.class,
                                    "LBL_SchemaTransform_"+selectedPattern.toString()),
                            NbBundle.getMessage(
                                    SchemaTransformPatternSelectionUI.class,
                                    "LBL_SchemaTransform_"+other.toString())});
                    return false;
                } else {
                    String warningMsgKey = "MSG_SchemaTransform_Warning_"+hint.toString();
                    setErrorMessage(warningMsgKey,
                            new Object[]{NbBundle.getMessage(
                                    SchemaTransformPatternSelectionUI.class,
                                    "LBL_SchemaTransform_"+selectedPattern.toString())});
                    return true;
                }
            }
        }
        setErrorMessage(null);
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }
    
    private SchemaGeneratorFactory.TransformHint canTransformSchema(
            final SchemaModel sm, final SchemaGenerator.Pattern inferedPattern, 
            final SchemaGenerator.Pattern selectedPattern, List<Element> ges) {
        SchemaGeneratorFactory.TransformHint hint = hintMap.get(selectedPattern);
        if(hint == null) {
            hint = SchemaGeneratorFactory.getDefault().canTransformSchema(
                    sm, inferedPattern, selectedPattern, ges);
            hintMap.put(selectedPattern, hint);
        }
        return hint;
    }
    
    private void setErrorMessage(String key) {
        if ( key == null ) {
            setLocalizedErrorMessage(""); // NOI18N
        } else {
            setLocalizedErrorMessage(
                    NbBundle.getMessage(SchemaTransformPatternSelection.class, key)); // NOI18N
        }
    }
    
    private void setErrorMessage(String key, Object[] params) {
        if ( key == null ) {
            setLocalizedErrorMessage(""); // NOI18N
        } else {
            setLocalizedErrorMessage(
                    NbBundle.getMessage(SchemaTransformPatternSelection.class, key, params)); // NOI18N
        }
    }
    
    private void setLocalizedErrorMessage(String message) {
        wizard.putProperty("WizardPanel_errorMessage", message); // NOI18N
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wizard = (WizardDescriptor) settings;
        Boolean isSingleGlobalElementSelected =
                (Boolean) wizard.getProperty(SchemaTransformWizard.SINGLE_GLOBAL_ELEMENT_KEY);
        if(isSingleGlobalElementSelected != null)
            component.setSingleGlobalElementSelected(
                    isSingleGlobalElementSelected.booleanValue());
        
        Boolean isTypeReuseSelected =
                (Boolean) wizard.getProperty(SchemaTransformWizard.TYPE_REUSE_KEY);
        if(isTypeReuseSelected != null)
            component.setTypeReuseSelected(isTypeReuseSelected.booleanValue());
    }
    
    public void storeSettings(Object settings) {
        wizard = (WizardDescriptor) settings;
        wizard.putProperty(SchemaTransformWizard.SINGLE_GLOBAL_ELEMENT_KEY,
                Boolean.valueOf(component.isSingleGlobalElementSelected()));
        wizard.putProperty(SchemaTransformWizard.TYPE_REUSE_KEY,
                Boolean.valueOf(component.isTypeReuseSelected()));
    }
    
    public void stateChanged(ChangeEvent e) {
        if(wizard != null)
            wizard.putProperty(SchemaTransformWizard.SELECTED_DESIGN_PATTERN_KEY,
                    component.getSelectedDesignPattern());
        fireChangeEvent();
    }
    
}

