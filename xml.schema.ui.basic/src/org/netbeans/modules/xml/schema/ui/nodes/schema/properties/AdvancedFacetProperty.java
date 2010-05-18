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
 * AdvancedFacetProperty.java
 *
 * Created on April 19, 2006, 11:53 AM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import java.lang.reflect.InvocationTargetException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.TotalDigits;
import org.netbeans.modules.xml.schema.ui.basic.editors.StringEditor;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.newtype.SchemaComponentCreator;
import org.netbeans.modules.xml.xam.ui.XAMUtils;

/**
 *
 * @author Ajit Bhate
 */
public class AdvancedFacetProperty extends BaseSchemaProperty {
    
    private Class<? extends SchemaComponent> facetType;
    
    private SimpleTypeRestriction parent;
    
    /** Creates a new instance of AdvancedFacetProperty */
    public AdvancedFacetProperty(SimpleTypeRestriction parent,
            SchemaComponent facet,
            Class<? extends SchemaComponent> facetType,
            Class valueType,
            String property,
            String propDispName,
            String propDesc,
            Class propEditorClass)
            throws NoSuchMethodException {
        super(facet,
                valueType,
                facetType.getMethod(BaseSchemaProperty.
                firstLetterToUpperCase(property, "get"), new Class[0]),
                facetType.getMethod(BaseSchemaProperty.
                firstLetterToUpperCase(property, "set"), new Class[]{valueType}),
                property,propDispName,propDesc,
                valueType.equals(int.class)?StringEditor.class:propEditorClass);
        super.setName(propDispName);
        this.parent = parent;
        this.facetType = facetType;
    }
    
    public void setValue(Object val) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if(getComponent()==null && val == null) return;
        if(val==null) {
            parent.getModel().removeChildComponent(getComponent());
            setComponent(null);
            return;
        }
        if (super.getValueType().equals(int.class)) {
            int lowerLimit = TotalDigits.class.isAssignableFrom(facetType)?1:0;
            int newVal = lowerLimit-1;
            try {
                val = Integer.valueOf((String)val);
                newVal = ((Integer)val).intValue();
            } catch (Exception e) {
                // do nothing iae is thrown in such case
            }
            if(newVal<lowerLimit) {
                String msg = NbBundle.getMessage(AdvancedFacetProperty.class,
                        "MSG_Neg_Int_Value", val); //NOI18N
                IllegalArgumentException iae = new IllegalArgumentException(msg);
                ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                        msg, msg, null, new java.util.Date());
                throw iae;
            }
        }
        if(getComponent()==null) {
            SchemaComponent sc = createFacet();
            setComponent(sc);
            try {
                super.setValue(val);
                parent.getModel().addChildComponent(parent,getComponent(),-1);
            } catch (IllegalAccessException iae) {
                setComponent(null);
                throw iae;
            } catch (IllegalArgumentException iae) {
                setComponent(null);
                throw iae;
            } catch (InvocationTargetException ite) {
                setComponent(null);
                throw ite;
            } catch (Exception e) {
                setComponent(null);
                assert false;
            }
        } else {
            super.setValue(val);
        }
    }
    
    public Object getValue() throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if(getComponent()==null) return null;
        if (super.getValueType().equals(int.class)) {
            return super.getValue().toString();
        }
        return super.getValue();
    }
    
    /**
     * This api determines if this property is editable
     * @return Returns true if the property is editable, false otherwise.
     */
    public boolean canWrite() {
        if(getComponent()==null) {
            return XAMUtils.isWritable(parent.getModel());
        }
        return super.canWrite();
    }
    
    
    private SchemaComponent createFacet() {
        return SchemaComponentCreator.createComponent(
                parent.getModel().getFactory(),	facetType);
    }
}
