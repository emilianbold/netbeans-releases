/*
 * ArrayPropertyPanel.java
 *
 * Created on November 18, 2002, 5:39 PM
 */

package org.netbeans.test.editor.app.gui;

import java.awt.CardLayout;
import javax.swing.JComboBox;
import org.netbeans.test.editor.app.core.properties.ArrayProperty;

/**
 *
 * @author  eh103527
 */
public class ArrayPropertyPanel extends PropertyPanel {
    
    Object[] values;
    
    /** Creates a new instance of ArrayPropertyPanel */
    public ArrayPropertyPanel(ArrayProperty array,String name) {
        propertyName=name;
        property=array;
        values=array.getValues();
        initComponents();
        generateEdit();
        button.setText(property.getProperty());
        oldText=button.getText();
    }
    
    protected void buttonPressed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        if (!readOnly) {
            buttonState=false;
            oldText=button.getText();
            ((JComboBox)editField).setSelectedItem(property.getProperty());
            ((CardLayout)(getLayout())).show(this,"edit");
            evt.setSource(this);
            if (evt != null) {
                dialog.buttonPressed(evt);
            }
        }
    }
    
    protected void focusLost(java.awt.event.FocusEvent evt) {
        // Add your handling code here:
        buttonState=true;
        String s=(String)((JComboBox)editField).getSelectedItem();
        if (s == null)
            s="";
        property.setProperty(s);
        button.setText(property.getProperty());
        ((CardLayout)(getLayout())).show(this,"button");
        if (button.getText().compareTo(oldText) != 0) {
            dialog.propertyChanged(propertyName,property);
        }
    }
    
    protected void generateEdit() {
        generateArrayEdit();
        editField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ArrayPropertyPanel.this.focusLost(evt);
            }
        });
        editField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ArrayPropertyPanel.this.keyPressed(evt);
            }
        });
        add(editField, "edit");
    }
    
    private void generateArrayEdit() {
        editField=new JComboBox(values);
    }
    
}
