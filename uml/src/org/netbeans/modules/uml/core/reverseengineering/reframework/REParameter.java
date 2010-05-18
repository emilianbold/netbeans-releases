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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.util.List;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class REParameter extends ParserData implements IREParameter {
    /**
     * Specifies an expression whose evaluation yields a value to be used
     * when no argument is supplied for the Parameter.
     * @param pVal [out] The default value.
     */
    public String getDefaultValue() {
        Node node = getXMLNode("UML:Parameter.defaultValue");
        if (node != null) {
            // The first child node (Actually the only child node) will
            // be one of the expression nodes.  Since there are a number
            // of different expression nodes I can not do a xstring query.
            // However, every expression tag has the same interface.  I
            // need to retrieve the body attribute from the expression node.
            Node expr = XMLManip.getFirstChild(node);
            return expr != null? XMLManip.getAttributeValue(expr, "body") : null;
        }
        return null;
    }
    
    /**
     * Specifies what kind of a Parameter is required.  A parameter can be
     * an in parameter,  out parameter, or in/out parameter."
     * @param pVal [out] The parameter kind.
     */
    public int getKind() {
        String dir = XMLManip.getAttributeValue(getEventData(), "direction");
        int dirk = BaseElement.PDK_INOUT;
        if ("in".equals(dir))
            dirk = BaseElement.PDK_IN;
        else if ("out".equals(dir))
            dirk = BaseElement.PDK_OUT;
        else if ("result".equals(dir))
            dirk = BaseElement.PDK_RESULT;
        return dirk;
    }
    
    public ETList<IREMultiplicityRange> getMultiplicity() {
        
        REXMLCollection<IREMultiplicityRange> mul =
                new REXMLCollection<IREMultiplicityRange>(
                REMultiplicityRange.class,
                "UML:TypedElement.multiplicity/UML:Multiplicity" +
                "/UML:Multiplicity.range/UML:MultiplicityRange");
        try {
            mul.setDOMNode(getEventData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mul;
    }
    
    /**
     * Designates a type to which an argument value must conform.
     * @param pVal [out] The parameter type.
     */
    public String getType() {
        String retVal = XMLManip.getAttributeValue(getEventData(), "type");
        
        return retVal.replace(".", "::");
    }
    
    /**
     * Retrieve the name of the parameter.
     * @param pVal [out] The name of the parameter.
     */
    public String getName() {
        return XMLManip.getAttributeValue(getEventData(), "name");
    }
    
    /**
     * Specifies if the parameter is a primitive or an object instance.
     *
     * @param *pVal [out] True if primitive, False otherwise.
     */
    public boolean getIsPrimitive() {
        return Boolean.valueOf(getTokenDescriptorValue("IsPrimitive"))
        .booleanValue();
    }
    
    public boolean isTemplateType()
    {
        Node derivation = isDerivationPresent(getEventData());
        
        return derivation != null;
    }
    
    public boolean isCollectionType(ILanguage lang)
    {
        boolean retVal = false;
        
        Node type = isDerivationPresent(getEventData());
        String typeName = XMLManip.getAttributeValue(type, "name");
        
        return lang.isCollectionType(typeName);
    }
    
    protected Node isDerivationPresent(Node pNode)
    {
        Node ppDerivationElement = null;
        try
        {
            // Get derivations from TokenDescriptors
            Node spDerivationElement = pNode.selectSingleNode("./TokenDescriptors/TDerivation");
            ppDerivationElement = spDerivationElement;
        }
        catch (Exception e)
        {
            ppDerivationElement = null;
        }
        return ppDerivationElement;
    }
    
    public CollectionInformation getCollectionTypeInfo()
    {
        CollectionInformation retVal = null;
        
        
        
        return getCollectionTypeInfo(isDerivationPresent(getEventData()));
    }
    
    protected CollectionInformation getCollectionTypeInfo(Node type)
    {
        CollectionInformation retVal = null;
        
        if(type != null)
        {            
            String colectionName = XMLManip.getAttributeValue(type, "name");
            
            List params = type.selectNodes("./DerivationParameter");
            if(params.size() == 1)
            {
//                setDefaultRange(attr, fullName);
                
//              // Currently the Data is setup so that if one of the parameters
                // has derivation, then it is under the parent derivation not
                // the parameter.  It should be under the parameter.  However,
                // since we only have 1 parameter this is not a big deal at this
                // time.
                
                Node derivation = isDerivationPresent(type);
                if(derivation != null)
                {
                    retVal = getCollectionTypeInfo(derivation);
                    retVal.addCollectionName(colectionName);
                }
                else
                {
                    Node param = (Node) params.get(0);
                    String typeName = XMLManip.getAttributeValue(param, "value");
                    retVal = new CollectionInformation(typeName);
                    retVal.addCollectionName(colectionName);
                }
            }
        }
        
        return retVal;
    }
}
