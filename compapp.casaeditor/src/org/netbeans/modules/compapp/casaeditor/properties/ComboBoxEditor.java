/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * Created on May 13, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.compapp.casaeditor.properties;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * @author radval
 * @author jqian
 */
public class ComboBoxEditor<T> extends PropertyEditorSupport
        implements ExPropertyEditor {

    private PropertyEnv mEnv;
    /**
     * Describe variable <code>vals</code> here.
     *
     */
    private T[] vals = null;

    public ComboBoxEditor() {
    }

    /**
     * Creates a new <code>ListEditor</code> instance.
     *
     * @param values a <code>String[]</code> value
     */
    public ComboBoxEditor(T[] values) {
        setValues(values);
    }
    
    /**
     * Describe <code>setValues</code> method here.
     *
     * @param values a <code>String[]</code> value
     */
    public void setValues(T[] values) {
        vals = values;
    }

    public T[] getValues() {
        return vals;
    }

    /**
     * Returns all the values
     *
     * @return array of all the options
     */
    @Override
    public String[] getTags() {
        if (vals == null) {
            return new String[] {""};
        }
        
        String[] ret = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            ret[i] = vals[i].toString();
        }
        return ret;
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

    @Override
    public void setValue(Object t) {
        if (t == null || vals == null) {
            return;
        }
        if (!Arrays.asList(vals).contains(t)) {
            return; //throw new IllegalArgumentException("Illegal argument: " + t);
        }
        super.setValue(t);
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
