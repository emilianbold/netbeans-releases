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
 * NamespaceProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.basic.editors.NamespaceEditor;
import org.netbeans.modules.xml.schema.ui.nodes.schema.SchemaNode;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * This class provides property support for properties having namespace uris.
 * @author Ajit Bhate
 */
public class NamespaceProperty extends BaseSchemaProperty {
    
    private String typeDisplayName;
    /**
     * Creates a new instance of NamespaceProperty.
     * 
     * 
     * @param component The schema component which property belongs to.
     * @param property The property name.
     * @param propDispName The display name of the property.
     * @param propDesc Short description about the property.
     * @throws java.lang.NoSuchMethodException If no getter and setter for the property are found
     */
    public NamespaceProperty(SchemaComponent component,
            String property, String dispName, String desc, String typeDisplayName) 
            throws NoSuchMethodException {
            super(component,String.class,property,dispName,desc,null);
            this.typeDisplayName = typeDisplayName;
    }
    
    public void setValue(Object o) throws IllegalAccessException, InvocationTargetException {
        if(o==null) {
            super.setValue(null);
        } else if(o instanceof String) {
            try {
                new URI((String) o);
                super.setValue(o);
            } catch (URISyntaxException urse) {
                String msg = NbBundle.getMessage(SchemaNode.class, "MSG_Invalid_URI",o); //NOI18N
                IllegalArgumentException iae = new IllegalArgumentException(msg);
                ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                        msg, msg, urse, new java.util.Date());
                throw iae;
            }
        }
    }
    /**
     * This method returns the property editor.
     * Overridden to return special editor.
     */
    @Override
    public java.beans.PropertyEditor getPropertyEditor() {
        return new NamespaceEditor(super.getComponent(), typeDisplayName, 
                getDisplayName());
    }
    
}
