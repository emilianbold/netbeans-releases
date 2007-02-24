/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.project.ui.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
// import org.netbeans.modules.uml.project.ui.common.PanelCodeGen;
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectPanel;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel just asking for basic info.
 * @author Mike Frisino
 */
public final class PanelConfigureProject 
    implements WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel, 
        WizardDescriptor.FinishablePanel, PropertyChangeListener, ActionListener
{
    private int type;
    private WizardDescriptor wizardDescriptor;
    private PanelConfigureProjectVisual component;
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);

    /** Create the wizard panel descriptor. */
    public PanelConfigureProject(int type)
    {
        this.type = type;
    }
    
    public Component getComponent()
    {
        if (component == null)
            component = new PanelConfigureProjectVisual(this, this.type);
        
        return component;
    }
    
    public HelpCtx getHelp()
    {
        return new HelpCtx("uml_tasks_projects_creating_project"); // NOI18N
    }
    
    public boolean isValid()
    {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    

    
    public void readSettings(Object settings)
    {
        wizardDescriptor = (WizardDescriptor)settings;
        component.read(wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent)component)
            .getClientProperty("NewProjectWizard_Title"); // NOI18N
        
        if (substitute != null)
        {
            wizardDescriptor.putProperty
                ("NewProjectWizard_Title", substitute); // NOI18N
        }
    }
    
    public void storeSettings(Object settings)
    {
        WizardDescriptor wizDesc = (WizardDescriptor)settings;
        component.store(wizDesc);
         
        wizDesc.putProperty(
            NewUMLProjectWizardIterator.PROP_WIZARD_TITLE, null);
    }
    
    public boolean isFinishPanel()
    {
        
        // MCF - unfortunately, it looks like this is ONLY called when the
        // panel is entered ... not as dynamic as I was hoping.
        if (this.type == NewUMLProjectWizardIterator.TYPE_UML_JAVA)
        {
            if (isImplementationModeSelected())
            {
                // since the implementation mode is selected they must
                // go to the next panel to complete wizard
                return false;
            }
            
            else
                return true;
        }
        else
            return true;
    }
    
    public void validate() throws WizardValidationException
    {
        getComponent();
        component.validate(wizardDescriptor);
    }
    
    
    //// MCF - support for dynamically enabling/disabling the java project
    // panel based on whether the modeling mode is impl or not.
    public boolean isImplementationModeSelected()
    {
    	return ReferencedJavaProjectPanel.mIsImplementationMode;
    }
    
    
    public void propertyChange(PropertyChangeEvent evt)
    {
// IZ 84855 - conover - this is no longer valid with live RT disabled
//        if (PanelCodeGen.MODE_CHANGED_PROP.equals(evt.getPropertyName()))
//        {
//            if (UMLProject.PROJECT_MODE_IMPL_STR.equals(evt.getNewValue()))
//            {
//                // messageLabel.setForeground(Color.RED);
//                // messageLabel.setText(NbBundle.getMessage(
//                // CustomizerModeling.class, "Implementation_Warning"));
//                // firePropertyChange(IMPLEMENTATION_MODE_PROP, mIsEnabled, false);
//                ReferencedJavaProjectPanel.mIsImplementationMode = true;
//            }
//            
//            else
//            {
//                //  messageLabel.setText("");
//                //   firePropertyChange(CustomizerPane.OK_ENABLE_PROPERTY, mIsEnabled, true);
//            	  ReferencedJavaProjectPanel.mIsImplementationMode = false;
//            }
//            
//            this.fireChangeEvent();
//        }

        
        if (evt.getPropertyName().equals(
                NewUMLProjectWizardIterator.PROP_WIZARD_TYPE))
        {
            if (((Integer)evt.getNewValue()).intValue() == 
                NewUMLProjectWizardIterator.TYPE_REVERSE_ENGINEER)
            {
                ReferencedJavaProjectPanel.mIsImplementationMode = true;
            }
            
            else
                ReferencedJavaProjectPanel.mIsImplementationMode = false;
            
            fireChangeEvent();
        }
        
        else if (evt.getPropertyName().equals(
                ReferencedJavaProjectPanel.ASSOCIATED_JAVA_PROJ_PROP) ||
            evt.getPropertyName().equals(
                ReferencedJavaProjectPanel.SOURCE_GROUP_CHANGED_PROP))
        {
            fireChangeEvent();
        }
        
//        else if (RoseImportProjectPanel.ROSE_MODEL_PROP.equals(evt.getPropertyName()))
//            fireChangeEvent();
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
//        if (actionEvent.getActionCommand().equals("OK"))
//        {
//            
//        }
    }

    public final void addChangeListener(ChangeListener l)
    {
        synchronized (listeners)
        {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l)
    {
        synchronized (listeners)
        {
            listeners.remove(l);
        }
    }
    
    public final void fireChangeEvent()
    {
        Iterator it;
        synchronized (listeners)
        {
            it = new HashSet(listeners).iterator();
        }
        
        ChangeEvent ev = new ChangeEvent(this);
        
        while (it.hasNext())
        {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
}
