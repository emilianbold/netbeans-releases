/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.iep.editor.tcg.ps;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.netbeans.modules.iep.editor.designer.JTextFieldFilter;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import java.beans.PropertyEditorSupport;
import org.openide.explorer.propertysheet.PropertyModel;



/**
 *
 * @author rdwivedi
 */
public class AlphaNumericNamePropertyEditor extends TcgComponentNodePropertyEditor 
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
            picker.setDocument(JTextFieldFilter.newAlphaNumericUnderscore());
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
