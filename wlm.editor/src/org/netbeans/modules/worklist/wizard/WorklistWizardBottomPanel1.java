/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

public class WorklistWizardBottomPanel1 implements WizardDescriptor.FinishablePanel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TaskPanel component;
    
    private DataObject dObj;
    
    private JTextField fileNameTextField;
        
    private String mErrorMessage;
    
    private String oldFileName = "";
    
    private TemplateWizard templateWizard;
    
    private TextChangeListener mListener = new TextChangeListener();
   
    
    public WorklistWizardBottomPanel1(DataObject folder) {
        this.dObj = folder;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new TaskPanel(this, this.dObj);
            component.getTaskNameTextField().getDocument().addDocumentListener(new TaskNameDocumentListener());
            component.addPropertyChangeListener(new WSDLOperationPropertyChangeListener() );
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid() {
        
        if(templateWizard != null) {
            templateWizard.putProperty ("WizardPanel_errorMessage", this.mErrorMessage); // NOI18N
        }
        
        return this.mErrorMessage == null;
        
        
        
        // If it is always OK to press Next or Finish, then:
        
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    
//    public final void addChangeListener(ChangeListener l) {}
//    public final void removeChangeListener(ChangeListener l) {}
//    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
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
        templateWizard = (TemplateWizard)settings;
        
        
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor descriptor = (WizardDescriptor) settings;
        descriptor.putProperty(WizardConstants.TASK_NAME, component.getTaskName());
        descriptor.putProperty(WizardConstants.TASK_OPERATION, component.getSelectedOperation());
    }
    
    public void setNameTF(JTextField nameTF) {
        //gui.attachFileNameListener(nameTF);
        if(nameTF != null) {
            nameTF.getDocument().removeDocumentListener(mListener);//remove existing one
            nameTF.getDocument().addDocumentListener(mListener);
            fileNameTextField = nameTF;
        }
    }

    
    
    private boolean isValidName(Document doc) {
        try {
            String text = doc.getText(0, doc.getLength());
            boolean isValid  = org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(text);
            if(!isValid) {
                mErrorMessage = "Name \"" + text + "\" is not a valid NCName";
            } else {
                mErrorMessage = null;
            }
            
        }  catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return mErrorMessage == null;
    }
    
    
    void validateTask() {
        String taskName = component.getTaskName();
        if(taskName == null || taskName.equals("")) {
            this.mErrorMessage = "Provide a valid task name.";
        } 
        
        validateOperation();
        
        
    }
    
    void validateOperation() {
        
        Operation operation = component.getSelectedOperation();
        if( operation == null) {
            this.mErrorMessage = "Select a valid request-reply wsdl operation.";
            
        } else {
            this.mErrorMessage = null;
        }
        
        fireChangeEvent();
    }
    
    private void validateFileName() {
        boolean validFileName = isValidName(this.fileNameTextField.getDocument());
        if(!validFileName) {
            fireChangeEvent();
        }

//        String taskNamePrefix = "Task";
        String taskNamePrefix = "";
        
        String newTaskName = this.fileNameTextField.getText() + taskNamePrefix;
        String oldTaskName = oldFileName + taskNamePrefix;
        
        String taskName = component.getTaskName();
      
        if(taskName == null || taskName.trim().equals("") || taskName.equals(oldTaskName)) {
            component.setTaskName(newTaskName);
        }
        
        oldFileName = this.fileNameTextField.getText();
        
    }
    
    class TextChangeListener implements DocumentListener {
         
         public void changedUpdate(DocumentEvent e) {
            validateFileName();
         }
         
         public void insertUpdate(DocumentEvent e) {
             validateFileName();
         }

         public void removeUpdate(DocumentEvent e) {
             validateFileName();
         }
 
    }
    
    class TaskNameDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            validateTask();
        }

        public void removeUpdate(DocumentEvent e) {
            validateTask();
        }

        public void changedUpdate(DocumentEvent e) {
            
        }
        
        
    }
    
    class WSDLOperationPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            validateOperation();
        }
        
    }
    
}

