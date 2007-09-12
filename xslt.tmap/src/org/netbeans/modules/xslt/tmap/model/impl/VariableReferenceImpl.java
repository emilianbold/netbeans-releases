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

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class VariableReferenceImpl extends TMapReferenceImpl<VariableDeclarator> 
        implements VariableReference
{
    public static final String DOT = "."; // NOI18N

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
        return varName+DOT+partRef;
    }

    public static String getVarName(String refStr) {
        String varName = null;
        int dotIndex = refStr == null ? -1 : refStr.lastIndexOf(DOT);
        
        if (dotIndex > 0) {
            varName = refStr.substring(0, dotIndex);
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
        String varName = null;
        String refStr = getRefString();
        int dotIndex = refStr == null ? -1 : refStr.lastIndexOf(DOT);
        
        if (dotIndex > 0) {
            varName = refStr.substring(0, dotIndex);
        }
        
        return varName;
    }
    
}
