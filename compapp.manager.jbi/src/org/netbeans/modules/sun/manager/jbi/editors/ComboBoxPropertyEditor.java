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

/*
 * Created on May 13, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.sun.manager.jbi.editors;

import java.beans.PropertyEditorSupport;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ComboBoxPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv mEnv;
        
        /**
         * Describe variable <code>vals</code> here.
         *
         */
        private String[] vals = null;

        /**
         * Creates a new <code>ListEditor</code> instance.
         *
         * @param values a <code>String[]</code> value
         */
        public ComboBoxPropertyEditor(String[] values) {
            setValues(values);
        }

        /**
         * Describe <code>setValues</code> method here.
         *
         * @param values a <code>String[]</code> value
         */
        public void setValues(String[] values) {
            vals = values;
        }

        /**
         * Returns all the values
         *
         * @return array of [yes, no]
         */
        @Override
        public String[] getTags() {
            return vals;
        }

        /**
         * The special case here is, if there is no value set, then it
         * is a "no"
         *
         * @return yes or no
         */
        @Override
        public String getAsText() {
            if (getValue() == null) {
/*                if(vals != null && vals.length != 0) {
                    return vals[0];
                }*/
                return "";
            }
            return getValue().toString();
        }

        /**
         * Set the value from the PropertyEditor to the object
         *
         * @param t a <code>String</code> value
         */
        @Override
        public void setAsText(String t) {
            setValue(t);
        }

        /**
         * Describe <code>supportsCustomEditor</code> method here.
         *
         * @return a <code>boolean</code> value
         */
        @Override
        public boolean supportsCustomEditor() {
            return false;
        }

        /**
         * This method is called by the IDE to pass
         * the environment to the property editor.
         * @param env Environment passed by the ide.
         */
        public void attachEnv(PropertyEnv env) {
            mEnv = env;
        }
    
}
