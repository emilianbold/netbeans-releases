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

package com.netbeans.developer.modules.loaders.properties;

import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
                                              
public class PropertiesTableCellEditor extends DefaultCellEditor {
  /** Constructs a PropertiesTableCellEditor that uses a text field.
  * @param x  a JTextField object ...
  */
  public PropertiesTableCellEditor(JTextField tf, final JTextComponent commentComponent, 
                                   final JTextComponent valueComponent) {
    super(tf);
    this.clickCountToStart = 1;
    valueComponent.setDocument(tf.getDocument());
    this.delegate = new PropertiesEditorDelegate(commentComponent);
    ((JTextField)editorComponent).addActionListener(delegate);
  }                             
           
  /** Visible component */         
  JComponent getEditorComponent() {
    return editorComponent;
  }

    protected class PropertiesEditorDelegate extends DefaultCellEditor.EditorDelegate {
    
      JTextComponent commentComponent;           
      
      public PropertiesEditorDelegate(JTextComponent commentComponent) {
        this.commentComponent = commentComponent;
      }
      
      public void setValue(Object x) {
        super.setValue(x);
        if (x != null) {
          ((JTextField)getEditorComponent()).setText(x.toString());
          commentComponent.setText(((PropertiesTableModel.StringPair)x).getComment());
          if (((PropertiesTableModel.StringPair)x).isKeyType())
            commentComponent.setEnabled(false);
          else  
            commentComponent.setEnabled(true);
        }  
        else {
          ((JTextField)getEditorComponent()).setText("");
          commentComponent.setText("");
          commentComponent.setEnabled(true);
        }  
      }

      public Object getCellEditorValue() {
        return new PropertiesTableModel.StringPair(commentComponent.getText(), 
          ((JTextField)getEditorComponent()).getText());
      }

      public boolean startCellEditing(EventObject anEvent) {
        if(anEvent == null)
          getEditorComponent().requestFocus();
        return true;
      }

      public boolean stopCellEditing() {
        return true;
      }
    }

}

/*
 * <<Log>>
 */
