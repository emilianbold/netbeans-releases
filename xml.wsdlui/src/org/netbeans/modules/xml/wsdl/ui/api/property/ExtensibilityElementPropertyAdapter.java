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

package org.netbeans.modules.xml.wsdl.ui.api.property;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.NamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.property.XSDBooleanAttributeProperty;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.openide.util.NbBundle;

public class ExtensibilityElementPropertyAdapter extends PropertyAdapter {
    private ExtensibilityElement element;
    private String attributeName;
    private String defaultValue; 
    private boolean supportsDefaultValue;
    private String valueNotSetMessage = NbBundle.getMessage(XSDBooleanAttributeProperty.class, "LBL_ValueNotSet");
    private boolean isOptional;
    
    private NamedPropertyAdapter adapter;
    
    public ExtensibilityElementPropertyAdapter(ExtensibilityElement element, String name, boolean isOptional) {
        super(element);
        this.element = element;
        this.attributeName = name;
        this.isOptional = isOptional;
        
        //Provides refactoring support for name changes.
        if (isNamedReferenceable()) {
            if (name != null && name.equals(NamedReferenceable.NAME_PROPERTY)) {
                adapter = new ConstraintNamedPropertyAdapter(element) {
                
                    @Override
                    public boolean isNameExists(String name) {
                        // TODO Auto-generated method stub
                        return false;
                    }
                
                };
            }
        }

    }
    
    public ExtensibilityElementPropertyAdapter(ExtensibilityElement element, String name) {
        this(element, name, false);
    }
    
    public ExtensibilityElementPropertyAdapter(ExtensibilityElement element, String name, String defaultValue) {
        this(element, name, false);
        this.defaultValue = defaultValue;
        this.supportsDefaultValue = true;
    }
    
    /*
     * this is the default implementation, subclasses can override this.
     * @return value
     */
    public String getValue() {
        if (adapter != null) return adapter.getName();
        
        String value = element.getAttribute(attributeName);
        if (value == null) {
            value = "";
        }
        return value;
    }
    
    /*
     * this is the default implementation, subclasses can override this.
     * @param value the value to be set
     */
    public void setValue(String value) {
        if (value == null || value.trim().length() == 0 || value.equalsIgnoreCase(valueNotSetMessage)) {
            value = null;
        }
        
        if (adapter != null) adapter.setName(value);
        
        String oldValue = element.getAttribute(attributeName);
        
        if ((oldValue == null ^ value == null) || (oldValue != null && value != null && !oldValue.equals(value))) {
            boolean inTransaction = Utility.startTransaction(element.getModel());
            element.setAttribute(attributeName, value);
            Utility.endTransaction(element.getModel(), inTransaction);
        }
    }
    
    /*
     * generic setValue. if overridden, also override getValue
     * 
     * @param val
     */
    public void setValue(Object val) {
        
    }
    
    public ExtensibilityElement getExtensibilityElement() {
        return element;
    }
    
    
    public String getDefaultValue() {
        if (defaultValue == null) {
            return valueNotSetMessage;
        }
        return defaultValue;
    }
    
    public boolean supportsDefaultValue() {
        return supportsDefaultValue && isWritable();
    }
    
    public void setOptional(boolean bool) {
        isOptional = bool;
    }
    
    public boolean isOptional() {
        return isOptional;
    }
    
    public String getMessageForUnSet() {
        return valueNotSetMessage;
    }
    
    
    private boolean isNamedReferenceable() {
        return element instanceof NamedReferenceable;
    }
}
