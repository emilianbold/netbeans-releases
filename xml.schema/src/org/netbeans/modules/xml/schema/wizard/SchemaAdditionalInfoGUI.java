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

package org.netbeans.modules.xml.schema.wizard;

//java imports
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.loaders.TemplateWizard;

//netbeans imports
import org.openide.util.NbBundle;

/**
 * This class represents the schema wizard, that is the GUI.
 * Read http://performance.netbeans.org/howto/dialogs/wizard-panels.html.
 *
 * @author  Samaresh (Samaresh.Panda@Sun.Com)
 */
final class SchemaAdditionalInfoGUI extends javax.swing.JPanel {
    
    public static final String DEFAULT_TARGET_NAMESPACE = NbBundle.getMessage(SchemaAdditionalInfoGUI.class,"TXT_defaultTNS"); //NOI18N
    
    private static final long serialVersionUID = 1L;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private boolean namespaceChangedByUser;
    private JTextField fileField;
    WizardDescriptor.Panel parentPanel;
    
    /**
     * Creates new form SimpleTargetChooserGUI
     */
    public SchemaAdditionalInfoGUI() {
	initComponents();
	targetNamespaceTextField.setText(DEFAULT_TARGET_NAMESPACE);
	namespaceChangedByUser = false;
	targetNamespaceTextField.getDocument().addDocumentListener(
	    new DocumentListener(){
	    public void removeUpdate(DocumentEvent e) {
		namespaceChangedByUser = true;
	    }
	    
	    public void insertUpdate(DocumentEvent e) {
		namespaceChangedByUser = true;
	    }
	    
	    public void changedUpdate(DocumentEvent e) {
		// attribute change
	    }
	    
	});
    }
    
    /**
     * Returns the target namespace as entered by the user.
     */
    public String getTargetNamespace() {
	return targetNamespaceTextField.getText();
    }
    
    
    private JTextField findFileNameField(Component panel, String text) {
	Collection<Component> allComponents = new ArrayList<Component>();
	getAllComponents(new Component[] {panel}, allComponents);
	for (Component c : allComponents) {
	    // we assume that the first text field is the file text field
	    if (c instanceof JTextField) {
		JTextField tf = (JTextField) c;
		//if (text.equals(tf.getText())) {
		return tf;
		//}
	    }
	}
	return null;
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection<Component> allComponents ) {
	for( int i = 0; i < components.length; i++ ) {
	    if( components[i] != null ) {
		allComponents.add( components[i] );
		if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
		    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
		}
	    }
	}
    }
    
    void setParentPanel(WizardDescriptor.Panel panel) {
        this.parentPanel = panel;
    }
    
    public void attachListenerToFileName(TemplateWizard wizard) {
	if (fileField != null) {
	    return;
	}
        
	fileField = findFileNameField(
	    parentPanel.getComponent(),
	    Templates.getTemplate(wizard).getName());
	if (fileField != null) {
	    fileField.getDocument().addDocumentListener(
		new DocumentListener() {
		public void removeUpdate(DocumentEvent e) {
		    getText(e);
		}
		
		public void insertUpdate(DocumentEvent e) {
		    getText(e);
		}
		
		public void changedUpdate(DocumentEvent e) {
		    
		}
		
		private void getText(DocumentEvent e) {
		    try {
			String t =
			    e.getDocument().getText(0, e.getDocument().getLength());
			changeDefaultURL(t);
		    } catch (BadLocationException ex) {
			// ignore the event
		    }
		    
		}
	    }
	    );
	}
    }
    
    private void changeDefaultURL(String fileName) {
	if (!namespaceChangedByUser && (fileName != null)) {
	    String currentNamespace = targetNamespaceTextField.getText();
	    String generatedNamespace =
		currentNamespace.substring(0, currentNamespace.lastIndexOf("/")+1); //NOI18N
	    generatedNamespace += fileName;
	    targetNamespaceTextField.setText(generatedNamespace);
	    namespaceChangedByUser = false;
	}
    }
    
    /**
     * Allows addition of listeners.
     */
    public void addChangeListener(ChangeListener l) {
	listeners.add(l);
    }
    
    /**
     * Allows deletion of listeners.
     */
    public void removeChangeListener(ChangeListener l) {
	listeners.remove(l);
    }
    
//    /**
//     * Fires state change event.
//     */
//    private void fireChange() {
//        ChangeEvent e = new ChangeEvent(this);
//        List templist;
//        synchronized (this) {
//            templist = new ArrayList (listeners);
//        }
//        Iterator it = templist.iterator();
//        while (it.hasNext()) {
//            ((ChangeListener)it.next()).stateChanged(e);
//        }
//    }
    
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        targetNamespaceLabel = new javax.swing.JLabel();
        Mnemonics.setLocalizedText(targetNamespaceLabel, NbBundle.getMessage(SchemaAdditionalInfoGUI.class, "LBL_TargetNamespace"));
        targetNamespaceTextField = new javax.swing.JTextField();

        targetNamespaceLabel.setLabelFor(targetNamespaceTextField);
        targetNamespaceLabel.setText(org.openide.util.NbBundle.getMessage(SchemaAdditionalInfoGUI.class, "LBL_TargetNamespace")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(targetNamespaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(targetNamespaceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 359, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(targetNamespaceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(targetNamespaceLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel targetNamespaceLabel;
    private javax.swing.JTextField targetNamespaceTextField;
    // End of variables declaration//GEN-END:variables
    
}
