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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.staticanalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
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
public class BPELExtensionStaticAnalysisVisitor extends ValidationVisitor {
    
    public static final String VAL_INVALID_PROPERTY_ALIAS = "VAL_INVALID_PROPERTY_ALIAS"; //NOT I18N
    public static final String FIX_INVALID_PROPERTY_ALIAS = "VAL_INVALID_PROPERTY_ALIAS"; //NOT 118N

    public static final String VAL_INVALID_PROPERTY_ALIAS_MUST_SPECIFY_ONE_OF_MESSAGETYPE_OR_ELEMENT_OR_TYPE =
                            "VAL_INVALID_PROPERTY_ALIAS_MUST_SPECIFY_ONE_OF_MESSAGETYPE_OR_ELEMENT_OR_TYPE";

    public static final String VAL_INVALID_PROPERTY_ALIAS_MUST_SPECIFY_MESSAGE = 
                        "VAL_INVALID_PROPERTY_ALIAS_MUST_SPECIFY_MESSAGE";
    
    public static final String VAL_INVALID_PROPERTY_ALIAS_MUST_SPECIFY_MESSAGE_PART = 
                            "VAL_INVALID_PROPERTY_ALIAS_MUST_SPECIFY_MESSAGE_PART";
            
    
    public static final String VAL_INVALID_PROPERTY_ALIAS_MESSAGE_PART_IS_NOT_FROM_MESSAGE =
                        "VAL_INVALID_PROPERTY_ALIAS_MESSAGE_PART_IS_NOT_FROM_MESSAGE";
    public static final String FIX_INVALID_PROPERTY_ALIAS_MESSAGE_PART_IS_NOT_FROM_MESSAGE =
                        "FIX_INVALID_PROPERTY_ALIAS_MESSAGE_PART_IS_NOT_FROM_MESSAGE";
    
    public static final String VAL_INVALID_PROPERTY = "VAL_INVALID_PROPERTY"; //NOT I18N
    public static final String FIX_INVALID_PROPERTY = "VAL_INVALID_PROPERTY"; //NOT 118N
    
    public static final String VAL_INVALID_PROPERTY_MUST_SPECIFY_ONE_OF_ELEMENT_OR_TYPE =
                        "VAL_INVALID_PROPERTY_MUST_SPECIFY_ONE_OF_ELEMENT_OR_TYPE";

    
            
    private Validator mValidator;

    public BPELExtensionStaticAnalysisVisitor(Validator validator) {
        this.mValidator = validator;
        init();
    }

    public void visit(PartnerLinkType c) {
        
    }

    public void visit(Role c) {
    }

    public void visit(CorrelationProperty c) {
        NamedComponentReference<GlobalElement> geRef = c.getElement();
        String element = c.getAttribute(new StringAttribute(CorrelationProperty.ELEMENT_PROPERTY));
    
        NamedComponentReference<GlobalType> gtRef = c.getType();
        String type = c.getAttribute(new StringAttribute(CorrelationProperty.TYPE_PROPERTY));
        
        // only one of element/type attribute should be specified
        if (element != null && type != null)
        {
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY),
                            NbBundle.getMessage(getClass(), FIX_INVALID_PROPERTY));
            
        }
        
        if(element == null && type == null) {
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_MUST_SPECIFY_ONE_OF_ELEMENT_OR_TYPE),
                            "");
        }
    }



    public void visit(PropertyAlias c) {
        NamedComponentReference<Message> msgRef = c.getMessageType();
        String messageType = c.getAttribute(new StringAttribute(PropertyAlias.MESSAGE_TYPE_PROPERTY));
        
        String part = c.getPart();
        
        NamedComponentReference<GlobalElement> geRef =  c.getElement();
        String element = c.getAttribute(new StringAttribute(PropertyAlias.ELEMENT_PROPERTY));
        
        NamedComponentReference<GlobalType> gtRef = c.getType();
        String type = c.getAttribute(new StringAttribute(PropertyAlias.TYPE_PROPERTY));
        
        // only one of messageType & part/element/type attribute should be specified
        if ((messageType != null && element != null)
                || (messageType != null && type != null)
                || (element != null && type != null))
        {
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_ALIAS),
                            NbBundle.getMessage(getClass(), FIX_INVALID_PROPERTY_ALIAS));
            
        }
        
        //at least one of messageType & part/element/type attribute should be specified
        if(messageType == null && part == null && element == null && type == null) {
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_ALIAS_MUST_SPECIFY_ONE_OF_MESSAGETYPE_OR_ELEMENT_OR_TYPE),
                            "");
            
        }
        
        
        if(messageType == null && part != null) {
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_ALIAS_MUST_SPECIFY_MESSAGE),
                            "");
            
        } else if (messageType != null && part == null) {
            addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_ALIAS_MUST_SPECIFY_MESSAGE_PART),
                            "");
            
        }
        
        if(messageType != null && part != null) {
            Message msg = msgRef.get();
            if(msg != null) {
                boolean foundPart = false;
                
                Collection<Part> parts =  msg.getParts();
                Iterator<Part> it = parts.iterator();
                while(it.hasNext()) {
                    Part p = it.next();
                    if(p.getName() != null && p.getName().equals(part)) {
                        foundPart = true;
                        break;
                    }
                }
                
                if(!foundPart) {
                    addNewResultItem(Validator.ResultType.ERROR,
                            c,
                            NbBundle.getMessage(getClass(), VAL_INVALID_PROPERTY_ALIAS_MESSAGE_PART_IS_NOT_FROM_MESSAGE, part, messageType),
                            NbBundle.getMessage(getClass(), FIX_INVALID_PROPERTY_ALIAS_MESSAGE_PART_IS_NOT_FROM_MESSAGE,  messageType));
                }
            }
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
