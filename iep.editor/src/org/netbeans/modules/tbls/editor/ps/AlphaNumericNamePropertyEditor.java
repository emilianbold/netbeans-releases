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


package org.netbeans.modules.tbls.editor.ps;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.netbeans.modules.iep.editor.designer.JTextFieldFilter;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;



/**
 *
 * @author rdwivedi
 */
public class AlphaNumericNamePropertyEditor extends SingleTcgComponentNodePropertyEditor 
             implements ExPropertyEditor, InplaceEditor.Factory {

    
    /** Creates a new instance of AlphaNumericNamePropertyEditor */
    public AlphaNumericNamePropertyEditor() {
         
    }
    
    public void attachEnv(PropertyEnv env) {
        super.attachEnv(env);
        env.registerInplaceEditorFactory(this);
}

private InplaceEditor ed = null;
public InplaceEditor getInplaceEditor() {
    if (ed == null) {
        ed = new InplaceTextField();
    }
    return ed;
}


/*   public Component getCustomEditor() {
        JTextField field = new JTextField();
        
        field.setDocument(JTextFieldFilter.newAlphaNumericUnderscore());
        return field;
    }
   
   public boolean supportsCustomEditor() {
    return true;
}

  */  


/*   public Component getCustomEditor() {
        JTextField field = new JTextField();
        
        field.setDocument(JTextFieldFilter.newAlphaNumericUnderscore());
        return field;
    }
   
   public boolean supportsCustomEditor() {
    return true;
}

  */  
    private static class InplaceTextField implements InplaceEditor {
        private final JTextField picker = new JTextField();
        private PropertyEditor editor = null;
        public InplaceTextField() {
            picker.setDocument(JTextFieldFilter.newAlphaNumericUnderscore(picker));
        }
        public void connect(PropertyEditor propertyEditor, PropertyEnv env) {
            editor = propertyEditor;
            reset();
        }

        public JComponent getComponent() {
            return picker;
        }

        public void clear() {
            //avoid memory leaks:
            editor = null;
            model = null;
        }

        public Object getValue() {
            return picker.getText();
        }

        public void setValue(Object object) {
            picker.setText((String)object);
        }

        public boolean supportsTextEntry() {
            return true;
        }

        public void reset() {
            String d = (String) editor.getValue();
            if (d != null) {
                picker.setText(d);
            }
        }

        public KeyStroke[] getKeyStrokes() {
            return new KeyStroke[0];
        }

        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        public PropertyModel getPropertyModel() {
            return model;
        }

        private PropertyModel model;
        public void setPropertyModel(PropertyModel propertyModel) {
            this.model = propertyModel;
        }

        public boolean isKnownComponent(Component component) {
            return component == picker || picker.isAncestorOf(component);
        }

        public void addActionListener(ActionListener actionListener) {
           //do nothing - not needed for this component
        }

        public void removeActionListener(ActionListener actionListener) {
           //do nothing - not needed for this component
        }

        
    }

    
    
   
    
}
