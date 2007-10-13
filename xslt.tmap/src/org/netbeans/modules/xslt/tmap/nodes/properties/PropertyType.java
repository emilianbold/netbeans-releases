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
package org.netbeans.modules.xslt.tmap.nodes.properties;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;

/**
 *
 * @author Vitaly Bychkov
 * @author nk160297
 * 
 * @version 1.0
 */
public enum PropertyType {
    NAME(String.class, StringPropEditor.class),
    FILE(String.class, StringPropEditor.class),
    DOCUMENTATION(String.class, StringPropEditor.class), 
    VERSION(String.class, StringPropEditor.class), 
    AUTHOR(String.class, StringPropEditor.class), 
    LANGUAGE(String.class, StringPropEditor.class), 
    TARGET_NAMESPACE(String.class, StringPropEditor.class), 
    QUERY_LANGUAGE(String.class, StringPropEditor.class), 
    EXPRESSION_LANGUAGE(String.class, StringPropEditor.class), 
    // ABSTRACT_PROCESS(TBoolean.class, TBooleanEditor.class), 
    FAULT_NAME_RO(QName.class, QNamePropEditor.class), // Read-only variant
//    PARTNER_LINK(BpelReference.class, ModelReferenceEditor.class), 
    PORT_TYPE(WSDLReference.class, ModelReferenceEditor.class), 
    OPERATION(WSDLReference.class, ModelReferenceEditor.class), 
    SOURCE(String.class, StringPropEditor.class), 
    RESULT(String.class, StringPropEditor.class),
    
//    PARTNER_LINK_TYPE(String.class, StringPropEditor.class),
//    ROLE(String.class, StringPropEditor.class),
    ROLE(WSDLReference.class, ModelReferenceEditor.class),
    
//    OPERATION(String.class, StringPropEditor.class),
    
//    INPUT(BpelReference.class, ModelReferenceEditor.class), 
//    OUTPUT(BpelReference.class, ModelReferenceEditor.class), 
    VARIABLE(String.class, StringPropEditor.class), 
//    EVENT_VARIABLE_NAME(String.class, StringPropEditor.class),
//    TRANSIENT_CONDITION(String.class, StringPropEditor.class), 
//    JOIN_CONDITION(String.class, StringPropEditor.class), 
//    WHILE_CONDITION(BooleanExpr.class), 
//    TIME_EXPRESSION(String.class, StringPropEditor.class), 
//    WSDL_FILE(String.class, StringPropEditor.class), 
    PARTNER_LINK_TYPE(WSDLReference.class, ModelReferenceEditor.class), 
//    MY_ROLE(WSDLReference.class, ModelReferenceEditor.class), 
//    PARTNER_ROLE(WSDLReference.class, ModelReferenceEditor.class), 
    // SUPPRESS_JOIN_FAILURE(TBoolean.class, TBooleanEditor.class), 
    // MESSAGE_EXCHANGE(BpelReference.class, ModelReferenceEditor.class), 
////    VARIABLE_STEREOTYPE(VariableStereotype.class, VariableStereotypeEditor.class), 
    VARIABLE_TYPE(Reference.class, ModelReferenceEditor.class),
    VARIABLE_TYPE_QNAME(QName.class, QNamePropEditor.class),
    CORRELATON_PROPERTY_TYPE(GlobalSimpleType.class), 
    CORRELATON_PROPERTY_TYPE_NAME(String.class, StringPropEditor.class), 
//    CORRELATON_PROPERTY_TYPE_NAME(QName.class, QNamePropEditor.class), 
//    CORR_PROPERTY(CorrelationProperty.class), 
    CORR_PROPERTY_NAME(QName.class, QNamePropEditor.class), 
    MESSAGE_TYPE(Message.class), 
    MESSAGE_TYPE_NAME(QName.class, QNamePropEditor.class), 
    PART(String.class, StringPropEditor.class); 
    
    private Class<?> myClass;
    private String myDisplayName;
    private Class myPropertyEditorClass;

    PropertyType(Class aClass) {
        this(aClass, null);
    }
    
    PropertyType(Class<?> aClass, Class propertyEditorClass) {
        this.myClass = aClass;
        this.myPropertyEditorClass = propertyEditorClass;
    }
    
    public Class<?> getPropertyClass() {
        return myClass;
    }
    
    public String getDisplayName() {
        if (myDisplayName == null) {
            try {
                myDisplayName = NbBundle.getMessage(PropertyType.class, 
                        this.toString());
            } catch (Exception ex) {
                myDisplayName = name();
            }
        }
        return myDisplayName;
    }
    
    public Class getPropertyEditorClass() {
        return myPropertyEditorClass;
    }
}
