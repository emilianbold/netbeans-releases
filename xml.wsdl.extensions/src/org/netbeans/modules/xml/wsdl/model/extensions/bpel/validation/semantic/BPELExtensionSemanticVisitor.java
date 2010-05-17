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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.semantic;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.StringAttribute;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.ValidationVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class BPELExtensionSemanticVisitor extends ValidationVisitor {
    
    public static final String VAL_INVALID_PROPERTY_ALIAS_MESSAGE_TYPE = "VAL_INVALID_PROPERTY_ALIAS_MESSAGE_TYPE"; //NOT I18N
    public static final String FIX_INVALID_PROPERTY_ALIAS_MESSAGE_TYPE = "FIX_INVALID_PROPERTY_ALIAS_MESSAGE_TYPE"; //NOT I18N
    
    public static final String VAL_INVALID_PROPERTY_ALIAS_ELEMENT = "VAL_INVALID_PROPERTY_ALIAS_ELEMENT"; //NOT I18N
    public static final String FIX_INVALID_PROPERTY_ALIAS_ELEMENT = "FIX_INVALID_PROPERTY_ALIAS_ELEMENT"; //NOT I18N
    
    public static final String VAL_INVALID_PROPERTY_ALIAS_TYPE = "VAL_INVALID_PROPERTY_ALIAS_TYPE"; //NOT I18N
    public static final String FIX_INVALID_PROPERTY_ALIAS_TYPE = "FIX_INVALID_PROPERTY_ALIAS_TYPE"; //NOT I18N
    
    public static final String VAL_INVALID_PORT_TYPE = "VAL_INVALID_PORT_TYPE"; //NOT I18N
    public static final String FIX_INVALID_PORT_TYPE = "FIX_INVALID_PORT_TYPE"; //NOT I18N
    
    public static final String VAL_INVALID_PARTNERLINK_TYPE = "VAL_INVALID_PARTNERLINK_TYPE"; //NOT I18N
    public static final String FIX_INVALID_PARTNERLINK_TYPE = "FIX_INVALID_PARTNERLINK_TYPE"; //NOT I18N
    
    public static final String VAL_INVALID_PROPERTY_NAME = "VAL_INVALID_PROPERTY_NAME";
    public static final String FIX_INVALID_PROPERTY_NAME = "FIX_INVALID_PROPERTY_NAME";
    
            
    private Validator mValidator;

    public BPELExtensionSemanticVisitor(Validator validator) {
        this.mValidator = validator;
        init();
    }

    public void visit(PartnerLinkType c) {
        Role role1 = c.getRole1();
        if(role1 != null) {
            visit(role1);
        }
        
        Role role2 = c.getRole2();
        if(role2 != null) {
            visit(role2);
        }
        
        //make sure there only two roles specified
        if(c.getChildren(Role.class).size() > 2) {
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PARTNERLINK_TYPE),
                            NbBundle.getMessage(getClass(), FIX_INVALID_PARTNERLINK_TYPE));
        }
    }

    public void visit(Role c) {
        //make sure role's portType if specified in accessible
        NamedComponentReference<PortType> portTypeRef = c.getPortType();
        String portType = c.getAttribute(new StringAttribute(Role.PORT_TYPE_PROPERTY));
        if((portTypeRef== null || portTypeRef.get() == null) && portType != null) {
            
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PORT_TYPE, portType),
                            NbBundle.getMessage(getClass(), FIX_INVALID_PORT_TYPE, portType));
        }
    }


    public void visit(PropertyAlias c) {
       
        //make sure propery if specified exists
        NamedComponentReference<CorrelationProperty> propertyRef = c.getPropertyName();
        String property = c.getAttribute(new StringAttribute(PropertyAlias.PROPERTY_NAME_PROPERTY));
        if((propertyRef == null || propertyRef.get() == null) && property != null) {
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_NAME, property),
                            NbBundle.getMessage(getClass(), FIX_INVALID_PROPERTY_NAME, property));
        
        }
        
        //make sure messageType if specified is accessible
        NamedComponentReference<Message> msgRef = c.getMessageType();
        String messageType = c.getAttribute(new StringAttribute(PropertyAlias.MESSAGE_TYPE_PROPERTY));
        if((msgRef== null || msgRef.get() == null) && messageType != null) {
            
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_ALIAS_MESSAGE_TYPE, messageType),
                            NbBundle.getMessage(getClass(), FIX_INVALID_PROPERTY_ALIAS_MESSAGE_TYPE, messageType));
        }
        
        //make sure element if specified is accessible
        NamedComponentReference<GlobalElement> geRef =  c.getElement();
        String element = c.getAttribute(new StringAttribute(PropertyAlias.ELEMENT_PROPERTY));
        if((geRef == null || geRef.get() == null) && element != null) {
            
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_ALIAS_ELEMENT, element),
                            NbBundle.getMessage(getClass(), FIX_INVALID_PROPERTY_ALIAS_ELEMENT, element));
        }
        
        //make sure type if specified is accessible
        NamedComponentReference<GlobalType> gtRef = c.getType();
        String type = c.getAttribute(new StringAttribute(PropertyAlias.TYPE_PROPERTY));
        if((gtRef == null || gtRef.get() == null) && type != null) {
            
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_ALIAS_TYPE, type),
                            NbBundle.getMessage(getClass(), FIX_INVALID_PROPERTY_ALIAS_TYPE, type));
        }
        
       
        
    }
    
    
    /**
     * Fires to-do events to listeners.
     * 
     * @param toDoEvent
     *            To-do event to fire.
     * @return <code>true</code> if more events can be accepted by the
     *         listener; <code>false</code> otherwise.
     */
    void addNewResultItem( Validator.ResultType type, 
                           Component component,
                           String desc, 
                           String correction )
    {
        ResultItem item = new Validator.ResultItem(mValidator, 
                                                   type, 
                                                   component, 
                                                   desc + correction);
        getResultItems().add(item);
    }
    

    
}
