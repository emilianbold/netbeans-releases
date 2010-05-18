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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.sun.util;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

import org.netbeans.modules.j2ee.sun.ide.editors.BooleanEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.DataSourceTypeEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.IsolationLevelEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.ValidationMethodEditor;
/**
 *
 */
public class EnhancedPropertyEditorFactory {
    
    private static EnhancedPropertyEditorFactory factory;
    
    
    /** 
     * Creates a new instance of EnhancedPropertyEditorFactory 
     */
    private EnhancedPropertyEditorFactory() {
    }
    
    
    /**
     *
     *
     */
    public static EnhancedPropertyEditorFactory getInstance() {
        if(factory == null) {
            factory = new EnhancedPropertyEditorFactory();
        }
        return factory;
    }
    
    
    /**
     * Returns the appropriate editor given the type of the value.
     *
     * @param valueType The type of the value to be displayed in a 
     *        Property Sheet.
     * @return An EnhancedPropertyEditor for the value type specified.
     */
    public EnhancedPropertyEditor getEnhancedPropertyEditor(
            final Object value, final String customType) {
        if(customType.equals(PropertyConstants.DATASOURCE_TYPE_FIELD))
            return new DataSourceTypeEditor();
        else if(customType.equals(PropertyConstants.ISOLATION_LEVEL_FIELD))
            return new IsolationLevelEditor(true);
        else if(customType.equals(PropertyConstants.VALIDATION_METHOD_FIELD))
            return new ValidationMethodEditor();
        else if(customType.equals(PropertyConstants.TRAX_SUPPORT_FIELD))
            return new ValidationMethodEditor(PropertyConstants.TRAX_SUPPORT_FIELD);
        else if(value instanceof Boolean) {
            return new BooleanEditor();
        } else {
            return new BooleanEditor();
        }

    }
    
}
