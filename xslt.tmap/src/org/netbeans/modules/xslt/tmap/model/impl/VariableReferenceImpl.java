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
package org.netbeans.modules.xslt.tmap.model.impl;

import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import static org.netbeans.modules.xslt.tmap.TMapConstants.*;
/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class VariableReferenceImpl extends TMapReferenceImpl<VariableDeclarator> 
        implements VariableReference
{
    VariableReferenceImpl( VariableDeclarator target , AbstractComponent parent, 
            String value , TMapReferenceBuilder.TMapResolver resolver )
    {
        super(target, VariableDeclarator.class , parent , value, resolver);
    }
    
    VariableReferenceImpl( AbstractComponent parent, 
            String value , TMapReferenceBuilder.TMapResolver resolver )
    {
        super( VariableDeclarator.class , parent , value, resolver );
    }

    public Variable getReferencedVariable() {
        VariableDeclarator varContainer = get();
        if (varContainer == null) {
            return null;
        }
        
        String varName = getVarName();
        if (varName == null) {
            return null;
        }
        
        Variable var = varContainer.getInputVariable();
        if (var == null || !varName.equals(var.getName())) {
            var = varContainer.getOutputVariable();
            var = var != null && varName.equals(var.getName()) ? var : null;
        }
        return var;
    }

    public Reference[] getReferences() {
        return new Reference[] {this, getPart()};
    }

    public <T extends ReferenceableWSDLComponent> WSDLReference<T> 
            createWSDLReference(T target, Class<T> type) 
    {
// TODO m | r        
//////        readLock();
//////        try {
            return WSDLReferenceBuilder.getInstance().build( target , type , 
                    (TMapComponentAbstract) this.get() );
//////        }
//////        finally {
//////            readUnlock();
//////        }   
    }

    public void setPart(WSDLReference<Part> part) {
        Attribute attr = getAttribute();
    
// TODO 
//////        writeLock();
//////        try {
        TMapComponentAbstract component = (TMapComponentAbstract)getParent();
        if (component != null) {
            WSDLReference<Part> old = getPart();
            
            String partRefStr = part.getRefString();
            String attrValue = component.getAttribute(getAttribute());
            
            Variable var = getReferencedVariable();
            String varName = var == null ? null : var.getName();
            assert varName != null;

            
// TODO m | r            
////            PropertyUpdateEvent event = preUpdateAttribute(attr.getName(), old,
////                   ref );
            
            component.setAttribute(attr.getName(), attr , 
                    getVarRefString(varName, partRefStr));
// TODO m | r            
////            getComponent().postGlobalEvent(event);
        }
//////        }
//////        catch (VetoException e) {
//////            assert false;
//////        }
//////        finally {
//////            writeUnlock();
//////        }    
    }

    public WSDLReference<Part> getPart() {
        Attribute attr = getAttribute();
        TMapComponentAbstract component = (TMapComponentAbstract)getParent();
        
        if ( component == null || component.getAttribute( attr ) == null ){
            return null;
        }
        
        WSDLReference<Part> ref = WSDLReferenceBuilder.getInstance().build( Part.class , 
                component , attr );
        return ref;

//        Variable var = getReferenced();
//        String refStr = getRefString();
//        String partName = null;
//        int dotIndex = refStr == null ? -1 : refStr.lastIndexOf(".");
//        if (dotIndex > 0) {
//            partName = refStr.substring(dotIndex+1);
//        }
//        
//        if (partName == null) {
//            return null;
//        }
//        
//        Message message = null;
//        if (var != null) {
//            WSDLReference<Message> messageRef = var.getMessage();
//            message = messageRef == null ? null : messageRef.get();
//        }
//        
//        Collection<Part> parts = null;
//        if (message != null) {
//            parts = message.getParts();
//        }
//        
//        if (parts != null && parts.size() > 0) {
//            for (Part part : parts) {
//                if (part != null && partName.equals(part.getName())) {
//                    return part;
//                }
//            }
//        }
//        
//        return null;
    }
    
    public static String getVarRefString(String varName, String partRef) {
        return DOLLAR_SIGN+varName+DOT+partRef;
    }

    public static String getVarName(String refStr) {
        String varName = null;
        int dotIndex = refStr == null ? -1 : refStr.lastIndexOf(DOT);
        
        varName = dotIndex > 0 ? refStr.substring(0, dotIndex) : refStr;
        
        if (varName != null && varName.startsWith(DOLLAR_SIGN)) {
            varName = varName.substring(1);
        }
        
        return varName;
    }

    public static String getPartName(String refStr) {
        String partName = null;
        int dotIndex = refStr == null ? -1 : refStr.lastIndexOf(DOT);
        
        if (dotIndex > 0 && dotIndex < refStr.length()-1) {
            partName = refStr.substring(dotIndex+1);
        }
        
        return partName;
    }

    private String getVarName() {
        return getVarName(getRefString());
    }
    
}
